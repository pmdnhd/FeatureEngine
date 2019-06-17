cat("\014")

sspectro1 <- function (wave, f, wl = 512, ovlp = 0, wn = "hanning", norm = TRUE,
          correction = "none")
{
  input <- inputw(wave = wave, f = f)
  wave1 <- input$w
  wave <- wave1/f
  rm(wave1)
  f <- input$f
  rm(input)
  # Compute the nber of samples in the wav
  n <- nrow(wave)
  step <- seq(1, n - wl, wl - (ovlp * wl/100)) # range from 1 to n - wl by step wl - (ovlp * wl/100)
  W <- ftwindow(wl = wl, wn = wn, correction = correction) # window generation
  z <- apply(as.matrix(step), 1, function(x) Mod(fft(wave[x:(wl +
                                                               x - 1), ] * W)))
  # Apply a window on each segment of the waveform, compute FFT, take the Modulus
  z <- z[2:(1 + wl/2), ] # dims of z = (nfft/2, step), remove first component of FFT
  if (norm) {
    z <- z/max(z)
  }
  return(z)
}

ACI1 <- function (wave, f, wl = 512, ovlp = 0, wn = "hamming", flim = NULL,
          nbwindows = 1, norm=TRUE)
{
  input <- inputw(wave = wave, f = f)
  wave <- input$w
  f <- input$f
  rm(input)
  z <- sspectro1(wave, f, wl=wl, wn = wn, ovlp = ovlp, norm=norm)
  if (!is.null(flim)) {
    flim <- flim * 1000 * wl/f
    z <- z[flim[1]:flim[2], ]
  }
  acis <- array(0, nbwindows)
  for (j in 1:nbwindows) {
    l <- dim(z)[2]
    # print(floor(l/nbwindows * (j - 1) + 1))
    # print(floor(l/nbwindows * j))
    print(floor(l/nbwindows * j) - floor(l/nbwindows * (j - 1)))
    z1 <- z[, floor(l/nbwindows * (j - 1) + 1):floor(l/nbwindows *
                                                       j)]
    t <- array(0, dim(z1) - c(0, 1))
    for (i in 1:(dim(t)[1])) {
      t[i, ] <- abs(diff(z1[i, ]))/sum(z1[i, ])
    }
    acis[j] <- sum(t)
  }
  
  return(c(sum(acis),acis))
}

s<-readWave("reduced.wav")
# duration <- 60*s@samp.rate
# s <- s[1:duration]
# s <- readWave("/home/paul/FeatureEngine-benchmark_1/test/resources/sounds/Example0_16_3587_1500.0_1.wav")
# print(ACI1(s, f = 32768, wl = 512, ovlp = 50, nbwindows = 4, flim=c(0.01, 1.0)))
# print(ACI1(s, f = 32768, wl = 512, ovlp = 0, nbwindows = 1))
print(ACI1(s, f = 32768, wl = 512, ovlp = 0, nbwindows = 5))
# ACI <- acoustic_complexity(s)
# print(ACI$AciTotAll_left)
# [1] 149.7846 149.4394 149.5519 149.5349 149.9200 149.2983 149.6516 149.4900 149.8062 150.7301 148.9882
# [12] 150.8759