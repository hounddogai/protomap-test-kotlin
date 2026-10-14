package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.numbers.roundTo

/** Names the band a capped locker manifest score falls in. */
fun classifyCappedLockerManifest(score: Double): String {
    if (score < 21.1) {
        return "ok"
    }
    if (score < 97.6) {
        return "high"
    }
    return "watch"
}

fun cappedLockerManifestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 48783.0, in order. */
fun cappedLockerManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48783.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
