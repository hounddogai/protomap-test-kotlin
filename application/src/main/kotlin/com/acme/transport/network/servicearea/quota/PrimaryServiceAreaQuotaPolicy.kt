package com.acme.transport.network.servicearea.quota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a primary service area quota code in its canonical PSAQ- form. */
fun normalizePrimaryServiceAreaQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSAQ-")) {
        return clean
    }
    return "PSAQ-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 71775.0, in order. */
fun primaryServiceAreaQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71775.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryServiceAreaQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryServiceAreaQuota(): Int {
    val policy = defaultPolicy("primary-service-area-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
