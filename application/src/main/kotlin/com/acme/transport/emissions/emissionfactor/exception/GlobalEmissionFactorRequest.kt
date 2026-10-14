package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global emission factor request is in its life cycle. */
enum class GlobalEmissionFactorRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global emission factor request. Its code is unique within a tenant. */
data class GlobalEmissionFactorRequest(
    var code: String,
    var name: String,
    var region: String,
    var fuelLiters: Double,
    var capacity: Int,
    var updatedAt: Int,
    var loadKg: Double,
    var revision: Int,
    var description: String,
    var summary: String,
    var status: GlobalEmissionFactorRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalEmissionFactorRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalEmissionFactorRequestStatus): Boolean {
        when (status) {
            GlobalEmissionFactorRequestStatus.OPEN -> return next == GlobalEmissionFactorRequestStatus.RESOLVED
            GlobalEmissionFactorRequestStatus.IN_REVIEW -> return next == GlobalEmissionFactorRequestStatus.REOPENED
            GlobalEmissionFactorRequestStatus.RESOLVED -> {
                when (next) {
                    GlobalEmissionFactorRequestStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalEmissionFactorRequestStatus.REOPENED -> return next == GlobalEmissionFactorRequestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalEmissionFactorRequestStatus) {
        if (!canMoveTo(next)) {
            error("global emission factor request $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun projectedValue(): Double = round((loadKg - capacity.toDouble()) * 100) / 100

    fun snapshot(): GlobalEmissionFactorRequest {
        return GlobalEmissionFactorRequest(
            code = code,
            name = name,
            region = region,
            fuelLiters = fuelLiters,
            capacity = capacity,
            updatedAt = updatedAt,
            loadKg = loadKg,
            revision = revision,
            description = description,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global emission factor request in status open. */
fun newGlobalEmissionFactorRequest(code: String, name: String): GlobalEmissionFactorRequest {
    return GlobalEmissionFactorRequest(
        code = code,
        name = name,
        region = "",
        fuelLiters = 0.0,
        capacity = 0,
        updatedAt = 0,
        loadKg = 0.0,
        revision = 0,
        description = "",
        summary = "",
        status = GlobalEmissionFactorRequestStatus.OPEN,
    )
}
