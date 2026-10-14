package com.acme.transport.lastmile.parcel.parcelreservation

/** Puts a parcel manifest code in its canonical PM- form. */
fun normalizeParcelManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PM-")) {
        return clean
    }
    return "PM-${clean.replace(" ", "-")}"
}

/** Names the band a parcel manifest score falls in. */
fun classifyParcelManifest(score: Double): String {
    if (score < 14.8) {
        return "high"
    }
    if (score < 41.4) {
        return "minimal"
    }
    return "elevated"
}
