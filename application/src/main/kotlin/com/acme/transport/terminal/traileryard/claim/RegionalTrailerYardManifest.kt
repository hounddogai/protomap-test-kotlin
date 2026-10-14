package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.text.normalize

/** Where a regional trailer yard manifest is in its life cycle. */
enum class RegionalTrailerYardManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional trailer yard manifest. Its code is unique within a tenant. */
data class RegionalTrailerYardManifest(
    var code: String,
    var label: String,
    var loadKg: Double,
    var summary: String,
    var segment: String,
    var currency: String,
    var status: RegionalTrailerYardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerYardManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerYardManifestStatus): Boolean {
        when (status) {
            RegionalTrailerYardManifestStatus.IDLE -> {
                when (next) {
                    RegionalTrailerYardManifestStatus.STOPPED -> return true
                }
                return false
            }
            RegionalTrailerYardManifestStatus.WARMING -> return next == RegionalTrailerYardManifestStatus.READY || next == RegionalTrailerYardManifestStatus.STOPPED
            RegionalTrailerYardManifestStatus.READY -> {
                when (next) {
                    RegionalTrailerYardManifestStatus.DRAINING, RegionalTrailerYardManifestStatus.STOPPED -> return true
                }
                return false
            }
            RegionalTrailerYardManifestStatus.DRAINING -> return next == RegionalTrailerYardManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalTrailerYardManifestStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer yard manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = loadKg

    fun snapshot(): RegionalTrailerYardManifest {
        return RegionalTrailerYardManifest(
            code = code,
            label = label,
            loadKg = loadKg,
            summary = summary,
            segment = segment,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional trailer yard manifest in status idle. */
fun newRegionalTrailerYardManifest(code: String, label: String): RegionalTrailerYardManifest {
    return RegionalTrailerYardManifest(
        code = code,
        label = label,
        loadKg = 0.0,
        summary = "",
        segment = "",
        currency = "",
        status = RegionalTrailerYardManifestStatus.IDLE,
    )
}
