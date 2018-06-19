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

package org.ode.engine.signal_processing

import org.ode.utils.test.ErrorMetrics.rmse
import org.scalatest.{FlatSpec, Matchers}


/**
 * Tests for Periodogram class
 *
 * @author Alexandre Degurse
 */
class TestPeriodogram extends FlatSpec with Matchers {

  private val maxRMSE = 1.1E-15


  /** fft values are generated with Matlab
  * s = [0:0.1:10]; s = s(:);
  * sig = 2 * cos(s) + 3 * sin(s);
  * fft = fft(sig)
  *
  * Or with python:
  * s = numpy.arange(0, 10.1, 0.1)
  * sig = 2 * np.cos(s) + 3 * np.sin(s)
  * fft = numpy.fft.fft(sig)
  */
  private val fft: Array[Double] = Array(
    43.5997045362902398,0.0000000000000000,69.4311955791687581,
    55.1989753722152585,-71.9765916749999946,-123.3671857233852478,
    -13.7939722254342882,-40.7756884800141250,-5.1964181553943245,
    -25.9416644354194652,-2.0325200532674108,-19.3509292989097830,
    -0.4800256426443177,-15.5212144111236157,0.4060066848069988,
    -12.9808760080945724,0.9622114215992505,-11.1564888461143408,
    1.3352903070181843,-9.7745511814384738,1.5981216854707947,
    -8.6867243487377532,1.7904426150235975,-7.8050413857634418,
    1.9354924742951614,-7.0737897752724113,2.0476291743182786,
    -6.4558544987934585,2.1361251237213499,-5.9254968600137152,
    2.2071914913104012,-5.4642740333697777,2.2651187478025485,
    -5.0586067357441449,2.3129493865169928,-4.6982641300903287,
    2.3528903835438615,-4.3753846308083517,2.3865746365599279,
    -4.0838228930161300,2.4152315342429094,-3.8187023969319256,
    2.4398010959858816,-3.5761015921325816,2.4610120826290842,
    -3.3528291239176178,2.4794365304247177,-3.1462598725442570,
    2.4955285138463519,-2.9542133725797797,2.5096521480494451,
    -2.7748623167537945,2.5221021173586351,-2.6066627734953931,
    2.5331189274108117,-2.4483003135766972,2.5429003767030069,
    -2.2986479531350321,2.5516102821424043,-2.1567329831869411,
    2.5593851848923150,-2.0217105588352196,2.5663395533761562,
    -1.8928424844726284,2.5725698559208054,-1.7694800316506123,
    2.5781577746191102,-1.6510499146173208,2.5831727605781682,
    -1.5370427586604216,2.5876740795753674,-1.4270035512194834,
    2.5917124601106254,-1.3205236809828766,2.5953314287474072,
    -1.2172342567863252,2.5985683976155638,-1.1168004637803797,
    2.6014555540214173,-1.0189167645035555,2.6040205908791898,
    -0.9233027911247690,2.6062873081570901,-0.8296998050605298,
    2.6082761090120954,-0.7378676235200999,2.6100044092578880,
    -0.6475819308345102,2.6114869748987859,-0.5586319068300138,
    2.6127361993912190,-0.4708181158776046,2.6137623298617481,
    -0.3839506092344532,2.6145736495601515,-0.2978472003818508,
    2.6151766222450892,-0.2123318786296736,2.6155760028938366,
    -0.1272333305784191,2.6157749180318430,-0.0423835423209304
  )

  private val fs: Double = 1000.0
  private val nfft: Int = 101


  it should "compute the same periodograms as matlab periodogram on a fake signal" in {
    /** Matlab code
    * s = [0:0.1:10]; s = s(:);
    * sig = 2 * cos(s) + 3 * sin(s);
    * [Pxx,F] = periodogram(sig,[],length(sig),1000);
    */

    val normalizationFactor = 1 / (nfft * fs)

    val periodogramClass: Periodogram = new Periodogram(nfft, normalizationFactor)
    val periodograms: Array[Double] = periodogramClass.compute(fft)

    val expectedPeriodograms: Array[Double] = Array(
      0.018821131046057503,0.155794411914756625,0.403962223018968447,
      0.036691691896538800,0.013860845848032056,0.007496823811848302,
      0.004775020226013218,0.003339960066646203,0.002483031568184747,
      0.001927224772328257,0.001544815303614363,0.001269789223587577,
      0.001065042235699044,0.000908333515744774,0.000785634526380712,
      0.000687722475070738,0.000608322080169557,0.000543038033676886,
      0.000488714531177963,0.000443036592423639,0.000404273888323465,
      0.000371111524459942,0.000342535516931416,0.000317753597900728,
      0.000296139390379196,0.000277192371903949,0.000260508710992309,
      0.000245759721304290,0.000232675737391845,0.000221033904805966,
      0.000210648833828754,0.000201365373747474,0.000193052975168369,
      0.000185601254086868,0.000178916474315712,0.000172918738164341,
      0.000167539728077807,0.000162720880414618,0.000158411900851216,
      0.000154569551931392,0.000151156659045972,0.000148141293053034,
      0.000145496096845198,0.000143197730168098,0.000141226412423654,
      0.000139565547442531,0.000138201417571006,0.000137122937103952,
      0.000136321457271676,0.000135790616778697,0.000135526233395330
    )

    rmse(periodograms, expectedPeriodograms) should be < maxRMSE
  }

  it should "compute the same periodograms as scipy periodogram on a fake signal" in {
    /** Python code:
    * s = numpy.arange(0,10.1,0.1)
    * sig = 2 * numpy.cos(s) + 3 * numpy.sin(s)
    * f, psdScipy = scipy.signal.periodogram(x=sig, fs=1000.0, window='boxcar', nfft=101, detrend=False, return_onesided=True, scaling='density')
    */

    // normalizationFactor is specific to scipy
    val normalizationFactor = 1 / (fs * nfft)

    val periodogramClass: Periodogram = new Periodogram(nfft, normalizationFactor)
    val periodograms: Array[Double] = periodogramClass.compute(fft)

    val expectedPeriodograms: Array[Double] = Array(
      1.8821131046057527e-02, 1.5579441191475596e-01,
      4.0396222301896817e-01, 3.6691691896538488e-02,
      1.3860845848032179e-02, 7.4968238118482532e-03,
      4.7750202260132407e-03, 3.3399600666461841e-03,
      2.4830315681847611e-03, 1.9272247723282536e-03,
      1.5448153036143695e-03, 1.2697892235875754e-03,
      1.0650422356990395e-03, 9.0833351574477631e-04,
      7.8563452638070661e-04, 6.8772247507074065e-04,
      6.0832208016954961e-04, 5.4303803367689680e-04,
      4.8871453117795482e-04, 4.4303659242364503e-04,
      4.0427388832345491e-04, 3.7111152445994691e-04,
      3.4253551693140413e-04, 3.1775359790073971e-04,
      2.9613939037918656e-04, 2.7719237190395918e-04,
      2.6050871099229976e-04, 2.4575972130430059e-04,
      2.3267573739183450e-04, 2.2103390480597828e-04,
      2.1064883382874243e-04, 2.0136537374748750e-04,
      1.9305297516835623e-04, 1.8560125408688174e-04,
      1.7891647431569923e-04, 1.7291873816435425e-04,
      1.6753972807779228e-04, 1.6272088041463075e-04,
      1.5841190085120351e-04, 1.5456955193140565e-04,
      1.5115665904595981e-04, 1.4814129305304848e-04,
      1.4549609684518173e-04, 1.4319773016811157e-04,
      1.4122641242363747e-04, 1.3956554744254634e-04,
      1.3820141757099112e-04, 1.3712293710396855e-04,
      1.3632145727165960e-04, 1.3579061677871304e-04,
      1.3552623339531348e-04
    )

    rmse(periodograms, expectedPeriodograms) should be < maxRMSE
  }

  it should "raise IllegalArgumentException when given a signal of the wrong length" in {
    val signal: Array[Double] = new Array[Double](42)
    val periodogramClass: Periodogram = new Periodogram(50, 1.0)

    an [IllegalArgumentException] should be thrownBy periodogramClass.compute(signal)
  }

}
