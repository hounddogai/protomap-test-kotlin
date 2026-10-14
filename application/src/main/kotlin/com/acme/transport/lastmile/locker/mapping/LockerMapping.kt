package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker mapping records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker mapping is in its life cycle. */
enum class LockerMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a locker mapping and its current status. */
data class LockerMapping(
    var code: String,
    var label: String,
    var weight: Double,
    var externalRef: String,
    var createdAt: Int,
    var status: LockerMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerMappingStatus): Boolean {
        when (status) {
            LockerMappingStatus.IDLE -> return next == LockerMappingStatus.WARMING
            LockerMappingStatus.WARMING -> {
                when (next) {
                    LockerMappingStatus.READY -> return true
                }
                return false
            }
            LockerMappingStatus.READY -> return next == LockerMappingStatus.DRAINING
            LockerMappingStatus.DRAINING -> {
                when (next) {
                    LockerMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerMappingStatus) {
        if (!canMoveTo(next)) {
            error("locker mapping $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"locker mapping"} $code"

    fun weightedValue(): Double = round(weight * createdAt.toDouble() * 100) / 100

    fun snapshot(): LockerMapping {
        return LockerMapping(
            code = code,
            label = label,
            weight = weight,
            externalRef = externalRef,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a locker mapping in status idle. */
fun newLockerMapping(code: String, label: String): LockerMapping {
    return LockerMapping(
        code = code,
        label = label,
        weight = 0.0,
        externalRef = "",
        createdAt = 0,
        status = LockerMappingStatus.IDLE,
    )
}
