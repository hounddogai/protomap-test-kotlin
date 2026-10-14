package com.acme.transport.lastmile.locker.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker quota records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker quota is in its life cycle. */
enum class LockerQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A locker quota as the locker team keeps it. */
data class LockerQuota(
    var code: String,
    var title: String,
    var capacity: Int,
    var batchSize: Int,
    var windowDays: Int,
    var stopCount: Int,
    var revision: Int,
    var minimum: Double,
    var vehicleId: String,
    var description: String,
    var loadKg: Double,
    var status: LockerQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerQuotaStatus): Boolean {
        when (status) {
            LockerQuotaStatus.NEW -> return next == LockerQuotaStatus.RUNNING
            LockerQuotaStatus.QUEUED -> return next == LockerQuotaStatus.RUNNING || next == LockerQuotaStatus.DONE
            LockerQuotaStatus.RUNNING -> {
                when (next) {
                    LockerQuotaStatus.FAILED -> return true
                }
                return false
            }
            LockerQuotaStatus.DONE -> {
                when (next) {
                    LockerQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerQuotaStatus) {
        if (!canMoveTo(next)) {
            error("locker quota $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun projectedLoad(): Double = round(revision.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): LockerQuota {
        return LockerQuota(
            code = code,
            title = title,
            capacity = capacity,
            batchSize = batchSize,
            windowDays = windowDays,
            stopCount = stopCount,
            revision = revision,
            minimum = minimum,
            vehicleId = vehicleId,
            description = description,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker quota in status new. */
fun newLockerQuota(code: String, title: String): LockerQuota {
    return LockerQuota(
        code = code,
        title = title,
        capacity = 0,
        batchSize = 0,
        windowDays = 0,
        stopCount = 0,
        revision = 0,
        minimum = 0.0,
        vehicleId = "",
        description = "",
        loadKg = 0.0,
        status = LockerQuotaStatus.NEW,
    )
}
