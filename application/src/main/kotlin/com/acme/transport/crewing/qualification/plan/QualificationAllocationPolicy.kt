package com.acme.transport.crewing.qualification.plan

/** Names the band a qualification allocation score falls in. */
fun classifyQualificationAllocation(score: Double): String {
    if (score < 18.1) {
        return "high"
    }
    if (score < 69.9) {
        return "ok"
    }
    return "watch"
}

/** Puts a qualification allocation code in its canonical QA- form. */
fun normalizeQualificationAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QA-")) {
        return clean
    }
    return "QA-${clean.replace(" ", "-")}"
}
