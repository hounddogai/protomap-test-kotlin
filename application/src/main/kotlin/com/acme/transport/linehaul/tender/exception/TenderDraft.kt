package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender draft is in its life cycle. */
enum class TenderDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tender draft, with what the planners need to act on it. */
data class TenderDraft(
    var code: String,
    var label: String,
    var name: String,
    var odometer: Int,
    var windowDays: Int,
    var status: TenderDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderDraftStatus): Boolean {
        when (status) {
            TenderDraftStatus.NEW -> {
                when (next) {
                    TenderDraftStatus.RUNNING -> return true
                }
                return false
            }
            TenderDraftStatus.QUEUED -> return next == TenderDraftStatus.DONE
            TenderDraftStatus.RUNNING -> {
                when (next) {
                    TenderDraftStatus.DONE -> return true
                }
                return false
            }
            TenderDraftStatus.DONE -> return next == TenderDraftStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TenderDraftStatus) {
        if (!canMoveTo(next)) {
            error("tender draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round(windowDays.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): TenderDraft {
        return TenderDraft(
            code = code,
            label = label,
            name = name,
            odometer = odometer,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tender draft in status new. */
fun newTenderDraft(code: String, label: String): TenderDraft {
    return TenderDraft(
        code = code,
        label = label,
        name = "",
        odometer = 0,
        windowDays = 0,
        status = TenderDraftStatus.NEW,
    )
}
