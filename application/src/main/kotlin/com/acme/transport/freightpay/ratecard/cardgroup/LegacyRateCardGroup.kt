package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy rate card group records of freightpay's rate card work, and the rules, stores, and calculations
// around them.

/** Where a legacy rate card group is in its life cycle. */
enum class LegacyRateCardGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy rate card group as the rate card team keeps it. */
data class LegacyRateCardGroup(
    var code: String,
    var name: String,
    var source: String,
    var batchSize: Int,
    var locale: String,
    var contactEmail: String,
    var rate: Double,
    var trailerId: String,
    var timezone: String,
    var etaMinutes: Int,
    var discount: Double,
    var status: LegacyRateCardGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRateCardGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRateCardGroupStatus): Boolean {
        when (status) {
            LegacyRateCardGroupStatus.CREATED -> return next == LegacyRateCardGroupStatus.PUBLISHED
            LegacyRateCardGroupStatus.VALIDATED -> {
                when (next) {
                    LegacyRateCardGroupStatus.PUBLISHED, LegacyRateCardGroupStatus.RETIRED -> return true
                }
                return false
            }
            LegacyRateCardGroupStatus.PUBLISHED -> return next == LegacyRateCardGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LegacyRateCardGroupStatus) {
        if (!canMoveTo(next)) {
            error("legacy rate card group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedExposure(): Double = round(batchSize.toDouble() * rate * 100) / 100

    fun snapshot(): LegacyRateCardGroup {
        return LegacyRateCardGroup(
            code = code,
            name = name,
            source = source,
            batchSize = batchSize,
            locale = locale,
            contactEmail = contactEmail,
            rate = rate,
            trailerId = trailerId,
            timezone = timezone,
            etaMinutes = etaMinutes,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a legacy rate card group in status created. */
fun newLegacyRateCardGroup(code: String, name: String): LegacyRateCardGroup {
    return LegacyRateCardGroup(
        code = code,
        name = name,
        source = "",
        batchSize = 0,
        locale = "",
        contactEmail = "",
        rate = 0.0,
        trailerId = "",
        timezone = "",
        etaMinutes = 0,
        discount = 0.0,
        status = LegacyRateCardGroupStatus.CREATED,
    )
}
