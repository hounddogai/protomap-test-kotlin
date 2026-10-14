package com.acme.transport.network.node.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global node override is in its life cycle. */
enum class GlobalNodeOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global node override as the node team keeps it. */
data class GlobalNodeOverride(
    var code: String,
    var name: String,
    var source: String,
    var ownerId: String,
    var tags: MutableList<String>,
    var driverId: String,
    var minimum: Double,
    var batchSize: Int,
    var category: String,
    var approved: Boolean,
    var status: GlobalNodeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalNodeOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalNodeOverrideStatus): Boolean {
        when (status) {
            GlobalNodeOverrideStatus.PLANNED -> return next == GlobalNodeOverrideStatus.IN_PROGRESS
            GlobalNodeOverrideStatus.SCHEDULED -> return next == GlobalNodeOverrideStatus.IN_PROGRESS || next == GlobalNodeOverrideStatus.COMPLETED
            GlobalNodeOverrideStatus.IN_PROGRESS -> return next == GlobalNodeOverrideStatus.COMPLETED || next == GlobalNodeOverrideStatus.CANCELLED
            GlobalNodeOverrideStatus.COMPLETED -> return next == GlobalNodeOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalNodeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global node override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global node override"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round(batchSize.toDouble() * minimum * 100) / 100

    fun snapshot(): GlobalNodeOverride {
        return GlobalNodeOverride(
            code = code,
            name = name,
            source = source,
            ownerId = ownerId,
            tags = tags,
            driverId = driverId,
            minimum = minimum,
            batchSize = batchSize,
            category = category,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global node override in status planned. */
fun newGlobalNodeOverride(code: String, name: String): GlobalNodeOverride {
    return GlobalNodeOverride(
        code = code,
        name = name,
        source = "",
        ownerId = "",
        tags = mutableListOf<String>(),
        driverId = "",
        minimum = 0.0,
        batchSize = 0,
        category = "",
        approved = false,
        status = GlobalNodeOverrideStatus.PLANNED,
    )
}
