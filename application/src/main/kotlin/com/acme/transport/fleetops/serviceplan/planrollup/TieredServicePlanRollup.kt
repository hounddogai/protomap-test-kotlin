package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered service plan rollup records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a tiered service plan rollup is in its life cycle. */
enum class TieredServicePlanRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered service plan rollup. Its code is unique within a tenant. */
data class TieredServicePlanRollup(
    var code: String,
    var title: String,
    var sequence: Int,
    var pallets: Int,
    var floorValue: Double,
    var ownerId: String,
    var active: Boolean,
    var batchSize: Int,
    var status: TieredServicePlanRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredServicePlanRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredServicePlanRollupStatus): Boolean {
        when (status) {
            TieredServicePlanRollupStatus.CREATED -> {
                when (next) {
                    TieredServicePlanRollupStatus.PUBLISHED, TieredServicePlanRollupStatus.RETIRED -> return true
                }
                return false
            }
            TieredServicePlanRollupStatus.VALIDATED -> return next == TieredServicePlanRollupStatus.PUBLISHED
            TieredServicePlanRollupStatus.PUBLISHED -> return next == TieredServicePlanRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredServicePlanRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered service plan rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((floorValue - pallets.toDouble()) * 100) / 100

    fun snapshot(): TieredServicePlanRollup {
        return TieredServicePlanRollup(
            code = code,
            title = title,
            sequence = sequence,
            pallets = pallets,
            floorValue = floorValue,
            ownerId = ownerId,
            active = active,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered service plan rollup in status created. */
fun newTieredServicePlanRollup(code: String, title: String): TieredServicePlanRollup {
    return TieredServicePlanRollup(
        code = code,
        title = title,
        sequence = 0,
        pallets = 0,
        floorValue = 0.0,
        ownerId = "",
        active = false,
        batchSize = 0,
        status = TieredServicePlanRollupStatus.CREATED,
    )
}
