package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling declaration override records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a rolling declaration override is in its life cycle. */
enum class RollingDeclarationOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling declaration override, with what the planners need to act on it. */
data class RollingDeclarationOverride(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var capacity: Int,
    var stopCount: Int,
    var title: String,
    var status: RollingDeclarationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDeclarationOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDeclarationOverrideStatus): Boolean {
        when (status) {
            RollingDeclarationOverrideStatus.IDLE -> return next == RollingDeclarationOverrideStatus.WARMING || next == RollingDeclarationOverrideStatus.READY
            RollingDeclarationOverrideStatus.WARMING -> {
                when (next) {
                    RollingDeclarationOverrideStatus.READY -> return true
                }
                return false
            }
            RollingDeclarationOverrideStatus.READY -> return next == RollingDeclarationOverrideStatus.DRAINING || next == RollingDeclarationOverrideStatus.STOPPED
            RollingDeclarationOverrideStatus.DRAINING -> return next == RollingDeclarationOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingDeclarationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("rolling declaration override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round(fuelLiters * stopCount.toDouble() * 100) / 100

    fun snapshot(): RollingDeclarationOverride {
        return RollingDeclarationOverride(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            timezone = timezone,
            attributes = attributes,
            capacity = capacity,
            stopCount = stopCount,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling declaration override in status idle. */
fun newRollingDeclarationOverride(code: String, name: String): RollingDeclarationOverride {
    return RollingDeclarationOverride(
        code = code,
        name = name,
        fuelLiters = 0.0,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        capacity = 0,
        stopCount = 0,
        title = "",
        status = RollingDeclarationOverrideStatus.IDLE,
    )
}
