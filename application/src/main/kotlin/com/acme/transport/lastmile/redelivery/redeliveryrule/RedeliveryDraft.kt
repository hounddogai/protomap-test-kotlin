package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery draft is in its life cycle. */
enum class RedeliveryDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A redelivery draft as the redelivery team keeps it. */
data class RedeliveryDraft(
    var code: String,
    var title: String,
    var shiftCode: String,
    var odometer: Int,
    var archived: Boolean,
    var etaMinutes: Int,
    var unit: String,
    var locale: String,
    var status: RedeliveryDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryDraftStatus): Boolean {
        when (status) {
            RedeliveryDraftStatus.REQUESTED -> {
                when (next) {
                    RedeliveryDraftStatus.GRANTED, RedeliveryDraftStatus.EXPIRED -> return true
                }
                return false
            }
            RedeliveryDraftStatus.GRANTED -> {
                when (next) {
                    RedeliveryDraftStatus.REVOKED -> return true
                }
                return false
            }
            RedeliveryDraftStatus.REVOKED -> {
                when (next) {
                    RedeliveryDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryDraftStatus) {
        if (!canMoveTo(next)) {
            error("redelivery draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"redelivery draft"} $code"

    fun weightedVolume(): Double = round((etaMinutes.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryDraft {
        return RedeliveryDraft(
            code = code,
            title = title,
            shiftCode = shiftCode,
            odometer = odometer,
            archived = archived,
            etaMinutes = etaMinutes,
            unit = unit,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a redelivery draft in status requested. */
fun newRedeliveryDraft(code: String, title: String): RedeliveryDraft {
    return RedeliveryDraft(
        code = code,
        title = title,
        shiftCode = "",
        odometer = 0,
        archived = false,
        etaMinutes = 0,
        unit = "",
        locale = "",
        status = RedeliveryDraftStatus.REQUESTED,
    )
}
