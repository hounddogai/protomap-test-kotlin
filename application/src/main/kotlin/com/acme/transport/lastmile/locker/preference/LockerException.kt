package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker exception is in its life cycle. */
enum class LockerExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A locker exception as the locker team keeps it. */
data class LockerException(
    var code: String,
    var name: String,
    var stopCount: Int,
    var distanceKm: Double,
    var carrier: String,
    var contactEmail: String,
    var capacity: Int,
    var priority: Int,
    var score: Double,
    var attributes: MutableMap<String, String>,
    var status: LockerExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerExceptionStatus): Boolean {
        when (status) {
            LockerExceptionStatus.NEW -> {
                when (next) {
                    LockerExceptionStatus.DONE -> return true
                }
                return false
            }
            LockerExceptionStatus.QUEUED -> {
                when (next) {
                    LockerExceptionStatus.DONE, LockerExceptionStatus.FAILED -> return true
                }
                return false
            }
            LockerExceptionStatus.RUNNING -> {
                when (next) {
                    LockerExceptionStatus.DONE, LockerExceptionStatus.FAILED -> return true
                }
                return false
            }
            LockerExceptionStatus.DONE -> {
                when (next) {
                    LockerExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerExceptionStatus) {
        if (!canMoveTo(next)) {
            error("locker exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker exception"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round(capacity.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): LockerException {
        return LockerException(
            code = code,
            name = name,
            stopCount = stopCount,
            distanceKm = distanceKm,
            carrier = carrier,
            contactEmail = contactEmail,
            capacity = capacity,
            priority = priority,
            score = score,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a locker exception in status new. */
fun newLockerException(code: String, name: String): LockerException {
    return LockerException(
        code = code,
        name = name,
        stopCount = 0,
        distanceKm = 0.0,
        carrier = "",
        contactEmail = "",
        capacity = 0,
        priority = 0,
        score = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = LockerExceptionStatus.NEW,
    )
}
