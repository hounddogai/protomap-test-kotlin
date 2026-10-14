package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card manifest is in its life cycle. */
enum class RateCardManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rate card manifest as the rate card team keeps it. */
data class RateCardManifest(
    var code: String,
    var name: String,
    var amount: Double,
    var expiresAt: Int,
    var revision: Int,
    var startsAt: Int,
    var active: Boolean,
    var carrier: String,
    var rate: Double,
    var priority: Int,
    var unit: String,
    var status: RateCardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardManifestStatus): Boolean {
        when (status) {
            RateCardManifestStatus.CREATED -> {
                when (next) {
                    RateCardManifestStatus.PUBLISHED, RateCardManifestStatus.RETIRED -> return true
                }
                return false
            }
            RateCardManifestStatus.VALIDATED -> return next == RateCardManifestStatus.PUBLISHED || next == RateCardManifestStatus.RETIRED
            RateCardManifestStatus.PUBLISHED -> {
                when (next) {
                    RateCardManifestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardManifestStatus) {
        if (!canMoveTo(next)) {
            error("rate card manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"rate card manifest"} $code"

    fun adjustedValue(): Double = round(startsAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): RateCardManifest {
        return RateCardManifest(
            code = code,
            name = name,
            amount = amount,
            expiresAt = expiresAt,
            revision = revision,
            startsAt = startsAt,
            active = active,
            carrier = carrier,
            rate = rate,
            priority = priority,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rate card manifest in status created. */
fun newRateCardManifest(code: String, name: String): RateCardManifest {
    return RateCardManifest(
        code = code,
        name = name,
        amount = 0.0,
        expiresAt = 0,
        revision = 0,
        startsAt = 0,
        active = false,
        carrier = "",
        rate = 0.0,
        priority = 0,
        unit = "",
        status = RateCardManifestStatus.CREATED,
    )
}
