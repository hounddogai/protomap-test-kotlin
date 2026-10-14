package com.acme.transport.kit.retry

// Retry policies with pluggable backoff.

/** Backoff says how many milliseconds to wait before the next attempt. */
interface Backoff {
    fun delay(attempt: Int): Int
}

data class ConstantBackoff(var millis: Int) : Backoff {
    override fun delay(attempt: Int): Int = millis
}

data class LinearBackoff(var step: Int, var cap: Int) : Backoff {
    override fun delay(attempt: Int): Int = minOf(step * (attempt + 1), cap)
}

data class ExponentialBackoff(var base: Int, var factor: Double, var cap: Int) : Backoff {
    override fun delay(attempt: Int): Int {
        var delay = base.toDouble()
        for (i in 0 until attempt) {
            delay = delay * factor
        }
        return minOf(delay.toInt(), cap)
    }
}

/** JitterBackoff spreads another backoff's delays by a stable pseudo-random amount. */
data class JitterBackoff(var inner: Backoff, var spread: Int, var salt: String) : Backoff {
    override fun delay(attempt: Int): Int {
        val base = inner.delay(attempt)
        if (spread <= 0) {
            return base
        }
        val noise = ("$salt:$attempt".hashCode() and Int.MAX_VALUE) % spread
        return base + noise
    }
}

/** Policy retries an operation up to attempts times. */
data class Policy(var attempts: Int, var backoff: Backoff) {
    /** Calls op until it succeeds and returns how many attempts it took. */
    fun run(op: (Int) -> Boolean): Int {
        var waited = 0
        for (attempt in 0 until attempts) {
            if (op(attempt)) {
                return attempt + 1
            }
            waited += backoff.delay(attempt)
        }
        error("gave up after $attempts attempts and $waited ms")
    }

    fun totalDelay(): Int {
        var total = 0
        for (attempt in 0 until attempts) {
            total += backoff.delay(attempt)
        }
        return total
    }
}

fun defaultPolicy(salt: String): Policy = Policy(5, JitterBackoff(ExponentialBackoff(100, 2.0, 5000), 50, salt))
