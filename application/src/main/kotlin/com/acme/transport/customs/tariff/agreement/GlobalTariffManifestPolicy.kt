package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a global tariff manifest code in its canonical GTM- form. */
fun normalizeGlobalTariffManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTM-")) {
        return clean
    }
    return "GTM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalTariffManifest(): Int {
    val policy = defaultPolicy("global-tariff-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
