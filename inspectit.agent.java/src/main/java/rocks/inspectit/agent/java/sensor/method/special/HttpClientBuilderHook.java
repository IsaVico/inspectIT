package rocks.inspectit.agent.java.sensor.method.special;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.tracing.core.adapter.http.proxy.HttpRequestInterceptorProxy;
import rocks.inspectit.agent.java.util.ReflectionCache;

/**
 * Hook that intercepts the addInterceptorFirst method of the {@link HttpAsyncClientBuilder}.
 *
 * @author Isabel Vico Peinado
 *
 */
public class HttpClientBuilderHook implements ISpecialHook {

	/**
	 * Fully qualified name for the HttpRequestInterceptor.
	 */
	private static final String HTTP_REQUEST_INTERCEPTOR_FQN = "org.apache.http.HttpRequestInterceptor";

	/**
	 * addInterceptorFirst method name.
	 */
	private static final String ADD_INTERCEPTOR_FIRST = "addInterceptorFirst";

	/**
	 * Reflection cache to use for method invocation.
	 */
	private final ReflectionCache cache = new ReflectionCache();

	/**
	 * {@link IRuntimeLinker} used to proxy the interceptor of the builder.
	 */
	private final IRuntimeLinker runtimeLinker;

	/**
	 * Class for the interceptor.
	 */
	Class<?> interceptorClass;

	/**
	 * Default constructor.
	 *
	 * @param runtimeLinker
	 *            Used for proxy the interceptor of the builder.
	 */
	public HttpClientBuilderHook(IRuntimeLinker runtimeLinker) {
		this.runtimeLinker = runtimeLinker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("PMD.AvoidCatchingThrowable")
	public Object beforeBody(long methodId, Object object, Object[] parameters, SpecialSensorConfig ssc) {
		if ((parameters != null) && (parameters.length == 0)) {
			if (interceptorClass == null) {
				interceptorClass = getClass(object);
			}

			if (interceptorClass != null) {
				Object newProxy = runtimeLinker.createProxy(HttpRequestInterceptorProxy.class, new HttpRequestInterceptorProxy(), object.getClass().getClassLoader());
				cache.invokeMethod(object.getClass(), ADD_INTERCEPTOR_FIRST, new Class<?>[] { interceptorClass }, object, new Object[] { newProxy }, null);
			}
		}
		return null;
	}

	/**
	 * Get the class of the object through Class.forName.
	 *
	 * @param object
	 *            Object to get the class.
	 * @return Returns the class of the object.
	 */
	Class<?> getClass(Object object) {
		try {
			return Class.forName(HTTP_REQUEST_INTERCEPTOR_FQN, false, object.getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object afterBody(long methodId, Object object, Object[] parameters, Object result, SpecialSensorConfig ssc) {
		return null;
	}
}
