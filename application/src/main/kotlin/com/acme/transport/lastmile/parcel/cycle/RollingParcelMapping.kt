package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling parcel mapping is in its life cycle. */
enum class RollingParcelMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling parcel mapping. Its code is unique within a tenant. */
data class RollingParcelMapping(
    var code: String,
    var title: String,
    var driverId: String,
    var score: Double,
    var amount: Double,
    var routeCode: String,
    var stopCount: Int,
    var status: RollingParcelMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingParcelMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingParcelMappingStatus): Boolean {
        when (status) {
            RollingParcelMappingStatus.NEW -> {
                when (next) {
                    RollingParcelMappingStatus.RUNNING -> return true
                }
                return false
            }
            RollingParcelMappingStatus.QUEUED -> {
                when (next) {
                    RollingParcelMappingStatus.RUNNING, RollingParcelMappingStatus.DONE -> return true
                }
                return false
            }
            RollingParcelMappingStatus.RUNNING -> {
                when (next) {
                    RollingParcelMappingStatus.DONE -> return true
                }
                return false
            }
            RollingParcelMappingStatus.DONE -> {
                when (next) {
                    RollingParcelMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingParcelMappingStatus) {
        if (!canMoveTo(next)) {
            error("rolling parcel mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round((score - amount) * 100) / 100

    fun snapshot(): RollingParcelMapping {
        return RollingParcelMapping(
            code = code,
            title = title,
            driverId = driverId,
            score = score,
            amount = amount,
            routeCode = routeCode,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling parcel mapping in status new. */
fun newRollingParcelMapping(code: String, title: String): RollingParcelMapping {
    return RollingParcelMapping(
        code = code,
        title = title,
        driverId = "",
        score = 0.0,
        amount = 0.0,
        routeCode = "",
        stopCount = 0,
        status = RollingParcelMappingStatus.NEW,
    )
}
