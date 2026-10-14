package com.acme.transport.lastmile.parcel.request

import com.acme.transport.kit.text.normalize

/** Where a parcel target is in its life cycle. */
enum class ParcelTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a parcel target and its current status. */
data class ParcelTarget(
    var code: String,
    var title: String,
    var lane: String,
    var startsAt: Int,
    var label: String,
    var status: ParcelTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelTargetStatus): Boolean {
        when (status) {
            ParcelTargetStatus.NEW -> {
                when (next) {
                    ParcelTargetStatus.DONE -> return true
                }
                return false
            }
            ParcelTargetStatus.QUEUED -> {
                when (next) {
                    ParcelTargetStatus.RUNNING, ParcelTargetStatus.FAILED -> return true
                }
                return false
            }
            ParcelTargetStatus.RUNNING -> {
                when (next) {
                    ParcelTargetStatus.FAILED -> return true
                }
                return false
            }
            ParcelTargetStatus.DONE -> return next == ParcelTargetStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ParcelTargetStatus) {
        if (!canMoveTo(next)) {
            error("parcel target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"parcel target"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): ParcelTarget {
        return ParcelTarget(
            code = code,
            title = title,
            lane = lane,
            startsAt = startsAt,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a parcel target in status new. */
fun newParcelTarget(code: String, title: String): ParcelTarget {
    return ParcelTarget(
        code = code,
        title = title,
        lane = "",
        startsAt = 0,
        label = "",
        status = ParcelTargetStatus.NEW,
    )
}
