package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global parcel batch is in its life cycle. */
enum class GlobalParcelBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global parcel batch. Its code is unique within a tenant. */
data class GlobalParcelBatch(
    var code: String,
    var name: String,
    var loadKg: Double,
    var retries: Int,
    var country: String,
    var fuelLiters: Double,
    var category: String,
    var status: GlobalParcelBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalParcelBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalParcelBatchStatus): Boolean {
        when (status) {
            GlobalParcelBatchStatus.REQUESTED -> return next == GlobalParcelBatchStatus.REVOKED || next == GlobalParcelBatchStatus.EXPIRED
            GlobalParcelBatchStatus.GRANTED -> {
                when (next) {
                    GlobalParcelBatchStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalParcelBatchStatus.REVOKED -> {
                when (next) {
                    GlobalParcelBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalParcelBatchStatus) {
        if (!canMoveTo(next)) {
            error("global parcel batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round(retries.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): GlobalParcelBatch {
        return GlobalParcelBatch(
            code = code,
            name = name,
            loadKg = loadKg,
            retries = retries,
            country = country,
            fuelLiters = fuelLiters,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global parcel batch in status requested. */
fun newGlobalParcelBatch(code: String, name: String): GlobalParcelBatch {
    return GlobalParcelBatch(
        code = code,
        name = name,
        loadKg = 0.0,
        retries = 0,
        country = "",
        fuelLiters = 0.0,
        category = "",
        status = GlobalParcelBatchStatus.REQUESTED,
    )
}
