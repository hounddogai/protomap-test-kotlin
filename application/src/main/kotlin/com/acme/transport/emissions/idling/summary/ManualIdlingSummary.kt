package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual idling summary is in its life cycle. */
enum class ManualIdlingSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One manual idling summary, with what the planners need to act on it. */
data class ManualIdlingSummary(
    var code: String,
    var title: String,
    var visible: Boolean,
    var attempts: Int,
    var endsAt: Int,
    var tags: MutableList<String>,
    var status: ManualIdlingSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIdlingSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIdlingSummaryStatus): Boolean {
        when (status) {
            ManualIdlingSummaryStatus.PLANNED -> {
                when (next) {
                    ManualIdlingSummaryStatus.COMPLETED, ManualIdlingSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            ManualIdlingSummaryStatus.SCHEDULED -> return next == ManualIdlingSummaryStatus.COMPLETED || next == ManualIdlingSummaryStatus.CANCELLED
            ManualIdlingSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    ManualIdlingSummaryStatus.COMPLETED, ManualIdlingSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            ManualIdlingSummaryStatus.COMPLETED -> {
                when (next) {
                    ManualIdlingSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualIdlingSummaryStatus) {
        if (!canMoveTo(next)) {
            error("manual idling summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((endsAt.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): ManualIdlingSummary {
        return ManualIdlingSummary(
            code = code,
            title = title,
            visible = visible,
            attempts = attempts,
            endsAt = endsAt,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual idling summary in status planned. */
fun newManualIdlingSummary(code: String, title: String): ManualIdlingSummary {
    return ManualIdlingSummary(
        code = code,
        title = title,
        visible = false,
        attempts = 0,
        endsAt = 0,
        tags = mutableListOf<String>(),
        status = ManualIdlingSummaryStatus.PLANNED,
    )
}
