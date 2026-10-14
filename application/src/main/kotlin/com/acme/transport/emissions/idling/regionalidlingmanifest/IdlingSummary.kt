package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling summary is in its life cycle. */
enum class IdlingSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a idling summary and its current status. */
data class IdlingSummary(
    var code: String,
    var title: String,
    var notes: String,
    var visible: Boolean,
    var retries: Int,
    var shiftCode: String,
    var ceiling: Double,
    var status: IdlingSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingSummaryStatus): Boolean {
        when (status) {
            IdlingSummaryStatus.REQUESTED -> return next == IdlingSummaryStatus.GRANTED || next == IdlingSummaryStatus.EXPIRED
            IdlingSummaryStatus.GRANTED -> {
                when (next) {
                    IdlingSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            IdlingSummaryStatus.REVOKED -> {
                when (next) {
                    IdlingSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingSummaryStatus) {
        if (!canMoveTo(next)) {
            error("idling summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveLoad(): Double = round((retries.toDouble() - ceiling) * 100) / 100

    fun snapshot(): IdlingSummary {
        return IdlingSummary(
            code = code,
            title = title,
            notes = notes,
            visible = visible,
            retries = retries,
            shiftCode = shiftCode,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a idling summary in status requested. */
fun newIdlingSummary(code: String, title: String): IdlingSummary {
    return IdlingSummary(
        code = code,
        title = title,
        notes = "",
        visible = false,
        retries = 0,
        shiftCode = "",
        ceiling = 0.0,
        status = IdlingSummaryStatus.REQUESTED,
    )
}
