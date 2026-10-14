package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.numbers.roundTo

fun primaryDeclarationForecastBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun primaryDeclarationForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
