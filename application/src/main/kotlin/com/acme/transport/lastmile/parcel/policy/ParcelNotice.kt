package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel notice is in its life cycle. */
enum class ParcelNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a parcel notice and its current status. */
data class ParcelNotice(
    var code: String,
    var label: String,
    var segment: String,
    var startsAt: Int,
    var batchSize: Int,
    var score: Double,
    var depot: String,
    var attempts: Int,
    var notes: String,
    var status: ParcelNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelNoticeStatus): Boolean {
        when (status) {
            ParcelNoticeStatus.PLANNED -> {
                when (next) {
                    ParcelNoticeStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ParcelNoticeStatus.SCHEDULED -> return next == ParcelNoticeStatus.CANCELLED
            ParcelNoticeStatus.IN_PROGRESS -> return next == ParcelNoticeStatus.CANCELLED
            ParcelNoticeStatus.COMPLETED -> {
                when (next) {
                    ParcelNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelNoticeStatus) {
        if (!canMoveTo(next)) {
            error("parcel notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((batchSize.toDouble() + score) * 100) / 100

    fun snapshot(): ParcelNotice {
        return ParcelNotice(
            code = code,
            label = label,
            segment = segment,
            startsAt = startsAt,
            batchSize = batchSize,
            score = score,
            depot = depot,
            attempts = attempts,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a parcel notice in status planned. */
fun newParcelNotice(code: String, label: String): ParcelNotice {
    return ParcelNotice(
        code = code,
        label = label,
        segment = "",
        startsAt = 0,
        batchSize = 0,
        score = 0.0,
        depot = "",
        attempts = 0,
        notes = "",
        status = ParcelNoticeStatus.PLANNED,
    )
}
