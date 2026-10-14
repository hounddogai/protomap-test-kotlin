package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped transit time notice is in its life cycle. */
enum class CappedTransitTimeNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a capped transit time notice and its current status. */
data class CappedTransitTimeNotice(
    var code: String,
    var title: String,
    var minimum: Double,
    var distanceKm: Double,
    var source: String,
    var archived: Boolean,
    var vehicleId: String,
    var active: Boolean,
    var attempts: Int,
    var status: CappedTransitTimeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTransitTimeNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTransitTimeNoticeStatus): Boolean {
        when (status) {
            CappedTransitTimeNoticeStatus.NEW -> {
                when (next) {
                    CappedTransitTimeNoticeStatus.QUEUED, CappedTransitTimeNoticeStatus.FAILED -> return true
                }
                return false
            }
            CappedTransitTimeNoticeStatus.QUEUED -> {
                when (next) {
                    CappedTransitTimeNoticeStatus.FAILED -> return true
                }
                return false
            }
            CappedTransitTimeNoticeStatus.RUNNING -> return next == CappedTransitTimeNoticeStatus.DONE || next == CappedTransitTimeNoticeStatus.FAILED
            CappedTransitTimeNoticeStatus.DONE -> return next == CappedTransitTimeNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedTransitTimeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("capped transit time notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round(distanceKm * minimum * 100) / 100

    fun snapshot(): CappedTransitTimeNotice {
        return CappedTransitTimeNotice(
            code = code,
            title = title,
            minimum = minimum,
            distanceKm = distanceKm,
            source = source,
            archived = archived,
            vehicleId = vehicleId,
            active = active,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped transit time notice in status new. */
fun newCappedTransitTimeNotice(code: String, title: String): CappedTransitTimeNotice {
    return CappedTransitTimeNotice(
        code = code,
        title = title,
        minimum = 0.0,
        distanceKm = 0.0,
        source = "",
        archived = false,
        vehicleId = "",
        active = false,
        attempts = 0,
        status = CappedTransitTimeNoticeStatus.NEW,
    )
}
