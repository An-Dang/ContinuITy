package org.continuity.workload.dsl;

import org.continuity.workload.dsl.visitor.ContinuityModelVisitor;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * A weak reference to a {@link ContinuityModelElement}. It holds the id to the element and can hold
 * the element itself. To resolve this element via the id, call
 * {@link WeakReference#resolve(ContinuityModelElement)}.
 *
 * @author Henning Schulz
 *
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIdentityReference(alwaysAsId = true)
public class WeakReference<T extends ContinuityModelElement> {

	@JsonProperty(value = "id", required = true)
	private final String id;

	private final Class<T> type;

	private T referred = null;

	private final ContinuityModelVisitor visitor = new ContinuityModelVisitor(this::checkAndSetElement);

	public WeakReference(String id, Class<T> type) {
		this.id = id;
		this.type = type;
	}

	/**
	 * Creates a new WeakReference to the passed {@link ContinuityModelElement}.
	 *
	 * @param referred
	 *            The element to be referred to.
	 * @return A WeakReference to the passed element.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ContinuityModelElement> WeakReference<T> create(T referred) {
		if (referred == null) {
			throw new IllegalArgumentException("Passed element is null!");
		}
		return new WeakReference<T>(referred.getId(), (Class<T>) referred.getClass());
	}

	/**
	 * Gets the id of the referred element.
	 *
	 * @return The referred id.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Gets the referred element, if set. To set the element, call
	 * {@link WeakReference#resolve(ContinuityModelElement)}.
	 *
	 * @return The referred element (can be {@code null}, if not yet resolved).
	 */
	public T getReferred() {
		return this.referred;
	}

	/**
	 * Resolves the referred element from the passed model, if possible and returns the reference.
	 *
	 * @param model
	 *            The model containing the referred element (can be nested).
	 * @return The resolved reference of {@code null}, if the reference wasn't found.
	 */
	public T resolve(ContinuityModelElement model) {
		if (referred == null) {
			visitor.visit(model);
		}

		return referred;
	}

	@SuppressWarnings("unchecked")
	private boolean checkAndSetElement(ContinuityModelElement referred) {
		if ((id != null) && id.equals(referred.getId())) {
			if (type.isAssignableFrom(referred.getClass())) {
				this.referred = (T) referred;
				return false;
			} else {
				throw new IllegalStateException("The reffered element is of type " + referred.getClass() + ". Expected " + type);
			}
		} else {
			return true;
		}
	}

}
