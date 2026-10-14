package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled accessorial draft is in its life cycle. */
enum class ScheduledAccessorialDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled accessorial draft as the accessorial team keeps it. */
data class ScheduledAccessorialDraft(
    var code: String,
    var name: String,
    var createdAt: Int,
    var fuelLiters: Double,
    var title: String,
    var status: ScheduledAccessorialDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAccessorialDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAccessorialDraftStatus): Boolean {
        when (status) {
            ScheduledAccessorialDraftStatus.NEW -> return next == ScheduledAccessorialDraftStatus.QUEUED || next == ScheduledAccessorialDraftStatus.FAILED
            ScheduledAccessorialDraftStatus.QUEUED -> {
                when (next) {
                    ScheduledAccessorialDraftStatus.RUNNING, ScheduledAccessorialDraftStatus.FAILED -> return true
                }
                return false
            }
            ScheduledAccessorialDraftStatus.RUNNING -> {
                when (next) {
                    ScheduledAccessorialDraftStatus.DONE, ScheduledAccessorialDraftStatus.FAILED -> return true
                }
                return false
            }
            ScheduledAccessorialDraftStatus.DONE -> return next == ScheduledAccessorialDraftStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledAccessorialDraftStatus) {
        if (!canMoveTo(next)) {
            error("scheduled accessorial draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round(createdAt.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): ScheduledAccessorialDraft {
        return ScheduledAccessorialDraft(
            code = code,
            name = name,
            createdAt = createdAt,
            fuelLiters = fuelLiters,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled accessorial draft in status new. */
fun newScheduledAccessorialDraft(code: String, name: String): ScheduledAccessorialDraft {
    return ScheduledAccessorialDraft(
        code = code,
        name = name,
        createdAt = 0,
        fuelLiters = 0.0,
        title = "",
        status = ScheduledAccessorialDraftStatus.NEW,
    )
}
