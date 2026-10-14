package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy trailer yard manifest is in its life cycle. */
enum class LegacyTrailerYardManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One legacy trailer yard manifest, with what the planners need to act on it. */
data class LegacyTrailerYardManifest(
    var code: String,
    var name: String,
    var contactEmail: String,
    var active: Boolean,
    var amount: Double,
    var windowDays: Int,
    var ratio: Double,
    var status: LegacyTrailerYardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerYardManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerYardManifestStatus): Boolean {
        when (status) {
            LegacyTrailerYardManifestStatus.PROPOSED -> {
                when (next) {
                    LegacyTrailerYardManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyTrailerYardManifestStatus.APPROVED -> return next == LegacyTrailerYardManifestStatus.REJECTED || next == LegacyTrailerYardManifestStatus.WITHDRAWN
            LegacyTrailerYardManifestStatus.REJECTED -> return next == LegacyTrailerYardManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyTrailerYardManifestStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer yard manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy trailer yard manifest"} $code"

    fun weightedValue(): Double = round((amount - windowDays.toDouble()) * 100) / 100

    fun snapshot(): LegacyTrailerYardManifest {
        return LegacyTrailerYardManifest(
            code = code,
            name = name,
            contactEmail = contactEmail,
            active = active,
            amount = amount,
            windowDays = windowDays,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy trailer yard manifest in status proposed. */
fun newLegacyTrailerYardManifest(code: String, name: String): LegacyTrailerYardManifest {
    return LegacyTrailerYardManifest(
        code = code,
        name = name,
        contactEmail = "",
        active = false,
        amount = 0.0,
        windowDays = 0,
        ratio = 0.0,
        status = LegacyTrailerYardManifestStatus.PROPOSED,
    )
}
