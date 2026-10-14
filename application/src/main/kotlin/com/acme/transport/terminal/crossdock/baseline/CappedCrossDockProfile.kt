package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped cross dock profile is in its life cycle. */
enum class CappedCrossDockProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped cross dock profile as the cross dock team keeps it. */
data class CappedCrossDockProfile(
    var code: String,
    var label: String,
    var channel: String,
    var pinned: Boolean,
    var phone: String,
    var ownerId: String,
    var category: String,
    var notes: String,
    var limit: Int,
    var floorValue: Double,
    var locale: String,
    var status: CappedCrossDockProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCrossDockProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCrossDockProfileStatus): Boolean {
        when (status) {
            CappedCrossDockProfileStatus.IDLE -> return next == CappedCrossDockProfileStatus.WARMING
            CappedCrossDockProfileStatus.WARMING -> {
                when (next) {
                    CappedCrossDockProfileStatus.STOPPED -> return true
                }
                return false
            }
            CappedCrossDockProfileStatus.READY -> {
                when (next) {
                    CappedCrossDockProfileStatus.DRAINING, CappedCrossDockProfileStatus.STOPPED -> return true
                }
                return false
            }
            CappedCrossDockProfileStatus.DRAINING -> {
                when (next) {
                    CappedCrossDockProfileStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCrossDockProfileStatus) {
        if (!canMoveTo(next)) {
            error("capped cross dock profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((floorValue + limit.toDouble()) * 100) / 100

    fun snapshot(): CappedCrossDockProfile {
        return CappedCrossDockProfile(
            code = code,
            label = label,
            channel = channel,
            pinned = pinned,
            phone = phone,
            ownerId = ownerId,
            category = category,
            notes = notes,
            limit = limit,
            floorValue = floorValue,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped cross dock profile in status idle. */
fun newCappedCrossDockProfile(code: String, label: String): CappedCrossDockProfile {
    return CappedCrossDockProfile(
        code = code,
        label = label,
        channel = "",
        pinned = false,
        phone = "",
        ownerId = "",
        category = "",
        notes = "",
        limit = 0,
        floorValue = 0.0,
        locale = "",
        status = CappedCrossDockProfileStatus.IDLE,
    )
}
