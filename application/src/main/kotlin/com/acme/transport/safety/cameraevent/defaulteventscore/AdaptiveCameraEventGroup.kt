package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive camera event group is in its life cycle. */
enum class AdaptiveCameraEventGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a adaptive camera event group and its current status. */
data class AdaptiveCameraEventGroup(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var fuelLiters: Double,
    var ratio: Double,
    var status: AdaptiveCameraEventGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCameraEventGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCameraEventGroupStatus): Boolean {
        when (status) {
            AdaptiveCameraEventGroupStatus.NEW -> {
                when (next) {
                    AdaptiveCameraEventGroupStatus.RUNNING, AdaptiveCameraEventGroupStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveCameraEventGroupStatus.QUEUED -> return next == AdaptiveCameraEventGroupStatus.RUNNING || next == AdaptiveCameraEventGroupStatus.DONE
            AdaptiveCameraEventGroupStatus.RUNNING -> {
                when (next) {
                    AdaptiveCameraEventGroupStatus.DONE, AdaptiveCameraEventGroupStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveCameraEventGroupStatus.DONE -> return next == AdaptiveCameraEventGroupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveCameraEventGroupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive camera event group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round(ratio * fuelLiters * 100) / 100

    fun snapshot(): AdaptiveCameraEventGroup {
        return AdaptiveCameraEventGroup(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            fuelLiters = fuelLiters,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive camera event group in status new. */
fun newAdaptiveCameraEventGroup(code: String, title: String): AdaptiveCameraEventGroup {
    return AdaptiveCameraEventGroup(
        code = code,
        title = title,
        durationMinutes = 0,
        fuelLiters = 0.0,
        ratio = 0.0,
        status = AdaptiveCameraEventGroupStatus.NEW,
    )
}
