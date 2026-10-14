package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock tier is in its life cycle. */
enum class CrossDockTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A cross dock tier as the cross dock team keeps it. */
data class CrossDockTier(
    var code: String,
    var name: String,
    var driverId: String,
    var createdAt: Int,
    var source: String,
    var minimum: Double,
    var floorValue: Double,
    var ceiling: Double,
    var attributes: MutableMap<String, String>,
    var status: CrossDockTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockTierStatus): Boolean {
        when (status) {
            CrossDockTierStatus.CREATED -> {
                when (next) {
                    CrossDockTierStatus.RETIRED -> return true
                }
                return false
            }
            CrossDockTierStatus.VALIDATED -> return next == CrossDockTierStatus.PUBLISHED || next == CrossDockTierStatus.RETIRED
            CrossDockTierStatus.PUBLISHED -> {
                when (next) {
                    CrossDockTierStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockTierStatus) {
        if (!canMoveTo(next)) {
            error("cross dock tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"cross dock tier"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((createdAt.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CrossDockTier {
        return CrossDockTier(
            code = code,
            name = name,
            driverId = driverId,
            createdAt = createdAt,
            source = source,
            minimum = minimum,
            floorValue = floorValue,
            ceiling = ceiling,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cross dock tier in status created. */
fun newCrossDockTier(code: String, name: String): CrossDockTier {
    return CrossDockTier(
        code = code,
        name = name,
        driverId = "",
        createdAt = 0,
        source = "",
        minimum = 0.0,
        floorValue = 0.0,
        ceiling = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = CrossDockTierStatus.CREATED,
    )
}
