package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped service plan rollup is in its life cycle. */
enum class CappedServicePlanRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped service plan rollup. Its code is unique within a tenant. */
data class CappedServicePlanRollup(
    var code: String,
    var name: String,
    var margin: Double,
    var segment: String,
    var tags: MutableList<String>,
    var floorValue: Double,
    var vehicleId: String,
    var retries: Int,
    var limit: Int,
    var threshold: Double,
    var status: CappedServicePlanRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServicePlanRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServicePlanRollupStatus): Boolean {
        when (status) {
            CappedServicePlanRollupStatus.CREATED -> {
                when (next) {
                    CappedServicePlanRollupStatus.VALIDATED, CappedServicePlanRollupStatus.RETIRED -> return true
                }
                return false
            }
            CappedServicePlanRollupStatus.VALIDATED -> return next == CappedServicePlanRollupStatus.PUBLISHED
            CappedServicePlanRollupStatus.PUBLISHED -> return next == CappedServicePlanRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedServicePlanRollupStatus) {
        if (!canMoveTo(next)) {
            error("capped service plan rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((margin - limit.toDouble()) * 100) / 100

    fun snapshot(): CappedServicePlanRollup {
        return CappedServicePlanRollup(
            code = code,
            name = name,
            margin = margin,
            segment = segment,
            tags = tags,
            floorValue = floorValue,
            vehicleId = vehicleId,
            retries = retries,
            limit = limit,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped service plan rollup in status created. */
fun newCappedServicePlanRollup(code: String, name: String): CappedServicePlanRollup {
    return CappedServicePlanRollup(
        code = code,
        name = name,
        margin = 0.0,
        segment = "",
        tags = mutableListOf<String>(),
        floorValue = 0.0,
        vehicleId = "",
        retries = 0,
        limit = 0,
        threshold = 0.0,
        status = CappedServicePlanRollupStatus.CREATED,
    )
}
