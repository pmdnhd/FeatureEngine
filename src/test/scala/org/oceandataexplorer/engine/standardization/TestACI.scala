/** Copyright (C) 2017-2018 Project-ODE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.oceandataexplorer.engine.standardization

import org.oceandataexplorer.engine.signalprocessing._
import org.oceandataexplorer.engine.signalprocessing.windowfunctions._
import WindowFunctionTypes.{Symmetric, Periodic}

import org.oceandataexplorer.utils.test.OdeCustomMatchers
import org.scalatest.{FlatSpec, Matchers}

/**
 * Automated signal processing tests for Scipy reference values
 * @author Alexandre Degurse
 */

class TestACI extends FlatSpec with Matchers with OdeCustomMatchers {

  /**
   * Maximum error allowed for [[OdeCustomMatchers.RmseMatcher]]
   */
  val maxRMSE = 1.0E-12

  val sound = SoundHandler("Sound1", 64, 24, 9811, 3906.0f, 1)



  it should "match seewave for 256/0/8" in {
    val winSize = 256
    val nfft = 256
    val overlap = 0

    val segmentation = Segmentation(winSize, overlap)
    val hammingSymmetric = HammingWindowFunction(winSize, Symmetric)
    val hammingPeriodic = HammingWindowFunction(winSize, Periodic)
    val hammingDensityPeriodicNormFactor = hammingPeriodic.densityNormalizationFactor()
    val hammingSpectrumPeriodicNormFactor = hammingPeriodic.spectrumNormalizationFactor()
    val fftClass = FFT(nfft, sound.samplingRate)
    val aciClass = AcousticComplexityIndex(8)

    val signal = sound.readSound()

    val ffts = segmentation.compute(signal)
      .map(segment => hammingSymmetric.applyToSignal(segment))
      .map(win => fftClass.compute(win))
      .map(fft => fft.map(_ / math.sqrt(hammingSymmetric.spectrumNormalizationFactor())))
    val aci = aciClass.compute(ffts)

    aci.foreach(println)
    println(aci.sum)

    val expectedACI = Array(
      48.166730946019008286,55.602743144976237488,94.304190522106466688,
      42.200831109723750956,58.316552773356434614,121.545672568161577942,
      96.390302082574947917,71.160372922484143032
    )

    aci should rmseMatch(expectedACI)
  }

  it should "match seewave for 256/138/15" in {
    val winSize = 256
    val nfft = 256
    val overlap = 138

    val segmentation = Segmentation(winSize, overlap)
    val hammingSymmetric = HammingWindowFunction(winSize, Symmetric)
    val hammingPeriodic = HammingWindowFunction(winSize, Periodic)
    val hammingDensityPeriodicNormFactor = hammingPeriodic.densityNormalizationFactor()
    val hammingSpectrumPeriodicNormFactor = hammingPeriodic.spectrumNormalizationFactor()
    val fftClass = FFT(nfft, sound.samplingRate)
    val aciClass = AcousticComplexityIndex(15)

    val signal = sound.readSound()

    val ffts = segmentation.compute(signal)
      .map(segment => hammingSymmetric.applyToSignal(segment))
      .map(win => fftClass.compute(win))
      .map(fft => fft.map(_ / math.sqrt(hammingSymmetric.spectrumNormalizationFactor())))
    val aci = aciClass.compute(ffts)

    aci.foreach(println)
    println(aci.sum)

    val expectedACI = Array(
      8.786702960802893259,24.940370637381700902,23.650719087612500857,
      107.131715637656256490,53.231419778614153415,53.563493661267301604,
      17.624966521810190301,27.881929214606930856,10.422575137604217943,
      84.095899057129884113,47.519965771289740530,18.827530658120085860,
      33.637846236140653389,73.863129608555539107,41.469778710868098415
    )

    aci should rmseMatch(expectedACI)
  }
}
