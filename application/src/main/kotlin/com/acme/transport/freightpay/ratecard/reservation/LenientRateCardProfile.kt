package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient rate card profile is in its life cycle. */
enum class LenientRateCardProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient rate card profile as the rate card team keeps it. */
data class LenientRateCardProfile(
    var code: String,
    var title: String,
    var batchSize: Int,
    var phone: String,
    var endsAt: Int,
    var status: LenientRateCardProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRateCardProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRateCardProfileStatus): Boolean {
        when (status) {
            LenientRateCardProfileStatus.CREATED -> {
                when (next) {
                    LenientRateCardProfileStatus.RETIRED -> return true
                }
                return false
            }
            LenientRateCardProfileStatus.VALIDATED -> {
                when (next) {
                    LenientRateCardProfileStatus.PUBLISHED, LenientRateCardProfileStatus.RETIRED -> return true
                }
                return false
            }
            LenientRateCardProfileStatus.PUBLISHED -> return next == LenientRateCardProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientRateCardProfileStatus) {
        if (!canMoveTo(next)) {
            error("lenient rate card profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round(batchSize.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): LenientRateCardProfile {
        return LenientRateCardProfile(
            code = code,
            title = title,
            batchSize = batchSize,
            phone = phone,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient rate card profile in status created. */
fun newLenientRateCardProfile(code: String, title: String): LenientRateCardProfile {
    return LenientRateCardProfile(
        code = code,
        title = title,
        batchSize = 0,
        phone = "",
        endsAt = 0,
        status = LenientRateCardProfileStatus.CREATED,
    )
}
