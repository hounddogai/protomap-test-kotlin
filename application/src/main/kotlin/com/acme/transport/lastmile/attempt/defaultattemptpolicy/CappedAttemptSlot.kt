package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped attempt slot is in its life cycle. */
enum class CappedAttemptSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped attempt slot. Its code is unique within a tenant. */
data class CappedAttemptSlot(
    var code: String,
    var name: String,
    var visible: Boolean,
    var etaMinutes: Int,
    var description: String,
    var country: String,
    var retries: Int,
    var threshold: Double,
    var endsAt: Int,
    var margin: Double,
    var cost: Double,
    var status: CappedAttemptSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAttemptSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAttemptSlotStatus): Boolean {
        when (status) {
            CappedAttemptSlotStatus.CREATED -> {
                when (next) {
                    CappedAttemptSlotStatus.VALIDATED -> return true
                }
                return false
            }
            CappedAttemptSlotStatus.VALIDATED -> return next == CappedAttemptSlotStatus.PUBLISHED
            CappedAttemptSlotStatus.PUBLISHED -> {
                when (next) {
                    CappedAttemptSlotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAttemptSlotStatus) {
        if (!canMoveTo(next)) {
            error("capped attempt slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped attempt slot"} $code"

    fun netTotal(): Double = round(threshold * cost * 100) / 100

    fun snapshot(): CappedAttemptSlot {
        return CappedAttemptSlot(
            code = code,
            name = name,
            visible = visible,
            etaMinutes = etaMinutes,
            description = description,
            country = country,
            retries = retries,
            threshold = threshold,
            endsAt = endsAt,
            margin = margin,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped attempt slot in status created. */
fun newCappedAttemptSlot(code: String, name: String): CappedAttemptSlot {
    return CappedAttemptSlot(
        code = code,
        name = name,
        visible = false,
        etaMinutes = 0,
        description = "",
        country = "",
        retries = 0,
        threshold = 0.0,
        endsAt = 0,
        margin = 0.0,
        cost = 0.0,
        status = CappedAttemptSlotStatus.CREATED,
    )
}
