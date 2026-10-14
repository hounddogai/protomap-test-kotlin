package com.acme.transport.linehaul.trailer.template

/** Puts a capped trailer preference code in its canonical CTP- form. */
fun normalizeCappedTrailerPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CTP-")) {
        return clean
    }
    return "CTP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 79156.0, in order. */
fun cappedTrailerPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79156.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a capped trailer preference score falls in. */
fun classifyCappedTrailerPreference(score: Double): String {
    if (score < 24.4) {
        return "low"
    }
    if (score < 53.8) {
        return "watch"
    }
    if (score < 91.6) {
        return "elevated"
    }
    return "ok"
}
