package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone cycle records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone cycle is in its life cycle. */
enum class ZoneCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A zone cycle. Its code is unique within a tenant. */
data class ZoneCycle(
    var code: String,
    var title: String,
    var retries: Int,
    var updatedAt: Int,
    var driverId: String,
    var cost: Double,
    var shiftCode: String,
    var loadKg: Double,
    var visible: Boolean,
    var status: ZoneCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneCycleStatus): Boolean {
        when (status) {
            ZoneCycleStatus.CREATED -> {
                when (next) {
                    ZoneCycleStatus.VALIDATED, ZoneCycleStatus.RETIRED -> return true
                }
                return false
            }
            ZoneCycleStatus.VALIDATED -> {
                when (next) {
                    ZoneCycleStatus.PUBLISHED, ZoneCycleStatus.RETIRED -> return true
                }
                return false
            }
            ZoneCycleStatus.PUBLISHED -> {
                when (next) {
                    ZoneCycleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneCycleStatus) {
        if (!canMoveTo(next)) {
            error("zone cycle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((retries.toDouble() + loadKg) * 100) / 100

    fun snapshot(): ZoneCycle {
        return ZoneCycle(
            code = code,
            title = title,
            retries = retries,
            updatedAt = updatedAt,
            driverId = driverId,
            cost = cost,
            shiftCode = shiftCode,
            loadKg = loadKg,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a zone cycle in status created. */
fun newZoneCycle(code: String, title: String): ZoneCycle {
    return ZoneCycle(
        code = code,
        title = title,
        retries = 0,
        updatedAt = 0,
        driverId = "",
        cost = 0.0,
        shiftCode = "",
        loadKg = 0.0,
        visible = false,
        status = ZoneCycleStatus.CREATED,
    )
}
