package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel profile is in its life cycle. */
enum class ParcelProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A parcel profile. Its code is unique within a tenant. */
data class ParcelProfile(
    var code: String,
    var name: String,
    var routeCode: String,
    var stopCount: Int,
    var etaMinutes: Int,
    var status: ParcelProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelProfileStatus): Boolean {
        when (status) {
            ParcelProfileStatus.CREATED -> {
                when (next) {
                    ParcelProfileStatus.PUBLISHED, ParcelProfileStatus.RETIRED -> return true
                }
                return false
            }
            ParcelProfileStatus.VALIDATED -> {
                when (next) {
                    ParcelProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            ParcelProfileStatus.PUBLISHED -> return next == ParcelProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ParcelProfileStatus) {
        if (!canMoveTo(next)) {
            error("parcel profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((stopCount.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): ParcelProfile {
        return ParcelProfile(
            code = code,
            name = name,
            routeCode = routeCode,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel profile in status created. */
fun newParcelProfile(code: String, name: String): ParcelProfile {
    return ParcelProfile(
        code = code,
        name = name,
        routeCode = "",
        stopCount = 0,
        etaMinutes = 0,
        status = ParcelProfileStatus.CREATED,
    )
}
