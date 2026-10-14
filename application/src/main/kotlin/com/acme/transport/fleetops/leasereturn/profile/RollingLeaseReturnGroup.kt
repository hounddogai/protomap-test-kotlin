package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling lease return group is in its life cycle. */
enum class RollingLeaseReturnGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling lease return group. Its code is unique within a tenant. */
data class RollingLeaseReturnGroup(
    var code: String,
    var name: String,
    var rate: Double,
    var carrier: String,
    var batchSize: Int,
    var durationMinutes: Int,
    var fuelLiters: Double,
    var threshold: Double,
    var windowDays: Int,
    var amount: Double,
    var status: RollingLeaseReturnGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLeaseReturnGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLeaseReturnGroupStatus): Boolean {
        when (status) {
            RollingLeaseReturnGroupStatus.CREATED -> return next == RollingLeaseReturnGroupStatus.PUBLISHED
            RollingLeaseReturnGroupStatus.VALIDATED -> {
                when (next) {
                    RollingLeaseReturnGroupStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingLeaseReturnGroupStatus.PUBLISHED -> return next == RollingLeaseReturnGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingLeaseReturnGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling lease return group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling lease return group"} $code"

    fun projectedLoad(): Double = round((batchSize.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): RollingLeaseReturnGroup {
        return RollingLeaseReturnGroup(
            code = code,
            name = name,
            rate = rate,
            carrier = carrier,
            batchSize = batchSize,
            durationMinutes = durationMinutes,
            fuelLiters = fuelLiters,
            threshold = threshold,
            windowDays = windowDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling lease return group in status created. */
fun newRollingLeaseReturnGroup(code: String, name: String): RollingLeaseReturnGroup {
    return RollingLeaseReturnGroup(
        code = code,
        name = name,
        rate = 0.0,
        carrier = "",
        batchSize = 0,
        durationMinutes = 0,
        fuelLiters = 0.0,
        threshold = 0.0,
        windowDays = 0,
        amount = 0.0,
        status = RollingLeaseReturnGroupStatus.CREATED,
    )
}
