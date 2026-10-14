package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedAvailabilitySlot(): Int {
    val policy = defaultPolicy("weighted-availability-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 34110.0, in order. */
fun weightedAvailabilitySlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34110.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted availability slot score falls in. */
fun classifyWeightedAvailabilitySlot(score: Double): String {
    if (score < 26.8) {
        return "minimal"
    }
    if (score < 75.8) {
        return "ok"
    }
    return "low"
}

/** Puts a weighted availability slot code in its canonical WAS- form. */
fun normalizeWeightedAvailabilitySlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAS-")) {
        return clean
    }
    return "WAS-${clean.replace(" ", "-")}"
}
