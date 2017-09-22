package rocks.inspectit.agent.java.tracing.core.adapter.http.proxy;


import rocks.inspectit.agent.java.eum.reflection.CachedMethod;
import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;
import rocks.inspectit.agent.java.tracing.core.adapter.error.ThrowableAwareResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.HttpResponseAdapter;
import rocks.inspectit.agent.java.tracing.core.adapter.http.data.HttpResponse;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;

/**
 * @author Isabel Vico Peinado
 *
 */
@ProxyFor(implementedInterfaces = "org.apache.http.concurrent.FutureCallback")
public class FutureCallbackProxy implements IProxySubject, HttpResponse {

	/**
	 * Span store that provides span that can be enriched.
	 */
	private SpanStore spanStore;

	/**
	 * Original object callback.
	 */
	private Object originalCallback;

	/**
	 * @param spanStore
	 *            Span store that provides span.
	 * @param originalCallBack
	 *            Original object callback.
	 */
	public FutureCallbackProxy(SpanStore spanStore, Object originalCallBack) {
		this.spanStore = spanStore;
		this.originalCallback = originalCallBack;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getProxyConstructorArguments() {
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void proxyLinked(Object proxyObject, IRuntimeLinker linker) {
	}

	/**
	 * Completed method for FutureCallback.
	 *
	 * @param response
	 *            Response of the request.
	 */
	@ProxyMethod(parameterTypes = { "java.lang.Object" })
	public void completed(Object response) {
		spanStore.finishSpan(new HttpResponseAdapter(this));

		if (originalCallback != null) {
			WFutureCallbackWrapper.ON_COMPLETED.call(originalCallback, response);
		}
	}

	/**
	 * Failed method of FutureCallback.
	 *
	 * @param exception
	 *            Exception thrown when the request failed.
	 */
	@ProxyMethod(parameterTypes = { "java.lang.Exception" })
	public void failed(Object exception) {
		spanStore.finishSpan(new ThrowableAwareResponseAdapter(exception.getClass().getSimpleName()));

		if (originalCallback != null) {
			WFutureCallbackWrapper.ON_FAILED.call(originalCallback, exception);
		}
	}

	/**
	 * Cancelled method for FutureCallback.
	 */
	@ProxyMethod()
	public void cancelled() {
		spanStore.finishSpan(new ThrowableAwareResponseAdapter());

		if (originalCallback != null) {
			WFutureCallbackWrapper.ON_FAILED.call(originalCallback);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStatus() {
		return 0;
	}

	/**
	 * Wrapper for the org.apache.http.concurrent.FutureCallback.
	 *
	 * @author Isabel Vico
	 *
	 */
	private interface WFutureCallbackWrapper {

		/**
		 * See {@link org.apache.http.concurrent.FutureCallback}.
		 */
		String CLAZZ = "org.apache.http.concurrent.FutureCallback";

		/**
		 * See {@link org.apache.http.concurrent.FutureCallback#cancelled()}.
		 */
		CachedMethod<Void> ON_CANCELLED = new CachedMethod<Void>(CLAZZ, "cancelled");

		/**
		 * See {@link org.apache.http.concurrent.FutureCallback#completed()}.
		 */
		CachedMethod<Void> ON_COMPLETED = new CachedMethod<Void>(CLAZZ, "completed", "java.lang.Object");

		/**
		 * See {@link org.apache.http.concurrent.FutureCallbackr#failed()}.
		 */
		CachedMethod<Void> ON_FAILED = new CachedMethod<Void>(CLAZZ, "failed", "java.lang.Exception");
	}
}
