package main.api;

/**
 * Interface that must be implemented in order to apply actions when a UAV reaches a target location.
 * <p>Developed by: Francisco Jos&eacute; Fabra Collado, from GRC research group in Universitat Polit&egrave;cnica de Val&egrave;ncia (Valencia, Spain).</p> */

public interface MoveToListener {

	/**
	 * Actions to perform when the take off has finished.
	 */
	abstract void onCompletedListener();
	
	/**
	 * Actions to perform if some error happens during the take off.
	 */
	abstract void onFailureListener();
}
