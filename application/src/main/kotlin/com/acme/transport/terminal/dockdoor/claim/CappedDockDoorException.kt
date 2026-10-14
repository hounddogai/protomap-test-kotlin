package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dock door exception is in its life cycle. */
enum class CappedDockDoorExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped dock door exception. Its code is unique within a tenant. */
data class CappedDockDoorException(
    var code: String,
    var title: String,
    var floorValue: Double,
    var distanceKm: Double,
    var approved: Boolean,
    var ceiling: Double,
    var externalRef: String,
    var dock: String,
    var status: CappedDockDoorExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDockDoorExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDockDoorExceptionStatus): Boolean {
        when (status) {
            CappedDockDoorExceptionStatus.IDLE -> {
                when (next) {
                    CappedDockDoorExceptionStatus.WARMING -> return true
                }
                return false
            }
            CappedDockDoorExceptionStatus.WARMING -> {
                when (next) {
                    CappedDockDoorExceptionStatus.DRAINING, CappedDockDoorExceptionStatus.STOPPED -> return true
                }
                return false
            }
            CappedDockDoorExceptionStatus.READY -> {
                when (next) {
                    CappedDockDoorExceptionStatus.STOPPED -> return true
                }
                return false
            }
            CappedDockDoorExceptionStatus.DRAINING -> return next == CappedDockDoorExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedDockDoorExceptionStatus) {
        if (!canMoveTo(next)) {
            error("capped dock door exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((distanceKm + ceiling) * 100) / 100

    fun snapshot(): CappedDockDoorException {
        return CappedDockDoorException(
            code = code,
            title = title,
            floorValue = floorValue,
            distanceKm = distanceKm,
            approved = approved,
            ceiling = ceiling,
            externalRef = externalRef,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped dock door exception in status idle. */
fun newCappedDockDoorException(code: String, title: String): CappedDockDoorException {
    return CappedDockDoorException(
        code = code,
        title = title,
        floorValue = 0.0,
        distanceKm = 0.0,
        approved = false,
        ceiling = 0.0,
        externalRef = "",
        dock = "",
        status = CappedDockDoorExceptionStatus.IDLE,
    )
}
