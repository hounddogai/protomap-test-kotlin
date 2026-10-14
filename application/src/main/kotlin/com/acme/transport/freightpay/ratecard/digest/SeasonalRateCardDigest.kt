package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.kit.text.normalize

// The seasonal rate card digest records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a seasonal rate card digest is in its life cycle. */
enum class SeasonalRateCardDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal rate card digest as the rate card team keeps it. */
data class SeasonalRateCardDigest(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var locale: String,
    var checksum: String,
    var driverId: String,
    var loadKg: Double,
    var name: String,
    var status: SeasonalRateCardDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRateCardDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRateCardDigestStatus): Boolean {
        when (status) {
            SeasonalRateCardDigestStatus.REQUESTED -> {
                when (next) {
                    SeasonalRateCardDigestStatus.GRANTED -> return true
                }
                return false
            }
            SeasonalRateCardDigestStatus.GRANTED -> {
                when (next) {
                    SeasonalRateCardDigestStatus.REVOKED, SeasonalRateCardDigestStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalRateCardDigestStatus.REVOKED -> {
                when (next) {
                    SeasonalRateCardDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRateCardDigestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal rate card digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal rate card digest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = loadKg

    fun snapshot(): SeasonalRateCardDigest {
        return SeasonalRateCardDigest(
            code = code,
            label = label,
            tags = tags,
            locale = locale,
            checksum = checksum,
            driverId = driverId,
            loadKg = loadKg,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal rate card digest in status requested. */
fun newSeasonalRateCardDigest(code: String, label: String): SeasonalRateCardDigest {
    return SeasonalRateCardDigest(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        locale = "",
        checksum = "",
        driverId = "",
        loadKg = 0.0,
        name = "",
        status = SeasonalRateCardDigestStatus.REQUESTED,
    )
}
