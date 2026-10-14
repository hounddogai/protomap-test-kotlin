package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.numbers.roundTo

/** Names the band a primary inspection plan score falls in. */
fun classifyPrimaryInspectionPlan(score: Double): String {
    if (score < 40.5) {
        return "elevated"
    }
    if (score < 58.0) {
        return "low"
    }
    return "medium"
}

fun primaryInspectionPlanBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
