package com.google.ar.core.codelab.Segmentation.translators;

import java.util.Objects;

import ai.djl.ndarray.NDArray;

public final class SamRawOutput {
	private final NDArray iouPred;
	private final NDArray lowResLogits;
	private final NDArray mask;

	SamRawOutput(NDArray iouPred, NDArray lowResLogits, NDArray mask) {
		this.iouPred = iouPred;
		this.lowResLogits = lowResLogits;
		this.mask = mask;
	}

	public NDArray iouPred() {
		return iouPred;
	}

	public NDArray lowResLogits() {
		return lowResLogits;
	}

	public NDArray mask() {
		return mask;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		SamRawOutput that = (SamRawOutput) obj;
		return Objects.equals(this.iouPred, that.iouPred) &&
				Objects.equals(this.lowResLogits, that.lowResLogits) &&
				Objects.equals(this.mask, that.mask);
	}

	@Override
	public int hashCode() {
		return Objects.hash(iouPred, lowResLogits, mask);
	}

	@Override
	public String toString() {
		return "SamRawOutput[" +
				"iouPred=" + iouPred + ", " +
				"lowResLogits=" + lowResLogits + ", " +
				"mask=" + mask + ']';
	}

	/**
	 * Close the NDArrays to prevent memory leaks.
	 */
	public void close() {
		iouPred.close();
		lowResLogits.close();
		mask.close();
	}
}
