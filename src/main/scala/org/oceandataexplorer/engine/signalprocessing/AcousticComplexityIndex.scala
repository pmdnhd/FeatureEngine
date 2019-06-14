package org.oceandataexplorer.engine.signalprocessing

case class AcousticComplexityIndex(
 nbWindows: Int
) {

  /**
    * Computes one-sided periodograms like in Matlab
    * and Python using the periodogram method (mode 'psd')
    * An IllegalArgumentException is thrown if
    * fft.length != 2*spectrumSize (ie fft is not one-sided)
    *
    * @param spectro The one-sided ffts used values gathered in a spectrogram
    * @return the one-sided periodogram
    */
  def compute(spectro: Array[Array[Double]]): Array[Double] = {
    // Extract the total nber of columns of the spectrogram
    val ncol = spectro.size
    // Divide the nber of columns into equal bins
    val times = (1 to nbWindows).map(j => ((ncol / nbWindows.toDouble * (j - 1)).toInt,
      (ncol / nbWindows.toDouble * j).toInt))
    // Transpose spectrogram to make computations easier
    val transposedSpectro = spectro.transpose
    // Array that will be filled by the ACI values of a bin
    var arrayACI = Array.fill(nbWindows)(0.0)
    var j = 1
    while (j <= nbWindows) {
      // sub-spectros of temporal size
      val subSpectros = transposedSpectro.map(row => row.slice(times(j - 1)._1, times(j - 1)._2))
      // Sum over all the frequencies of the sub-spectros (denominator of the ACI formula)
      val sums = subSpectros.map(_.sum)
      // "Compute absolute difference between two adjacent values of intensity (Ik and I(k+1)) in a single
      // frequency bin" Pieretti et al., 2011
      val diff = subSpectros.zipWithIndex.map(x => x._1.sliding(2)
        .map {
          case Array(a, b) =>
            scala.math.abs((a - b) /
              sums(x._2))
        }
      )
      // ACI for the sub-spectro
      arrayACI(j - 1) = diff.map(_.sum).sum
      j = j + 1
    }

    arrayACI
    // The total ACI is the sum of the computed ACIs
  }

}
