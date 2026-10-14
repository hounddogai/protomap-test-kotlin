package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeTieredLanePlanTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a tiered lane plan template code in its canonical TLPT- form. */
fun normalizeTieredLanePlanTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TLPT-")) {
        return clean
    }
    return "TLPT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 89306.0, in order. */
fun tieredLanePlanTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89306.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredLanePlanTemplate(): Int {
    val policy = defaultPolicy("tiered-lane-plan-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
