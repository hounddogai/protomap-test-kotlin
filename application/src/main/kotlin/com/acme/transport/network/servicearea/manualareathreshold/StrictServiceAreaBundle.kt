package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict service area bundle is in its life cycle. */
enum class StrictServiceAreaBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict service area bundle, with what the planners need to act on it. */
data class StrictServiceAreaBundle(
    var code: String,
    var name: String,
    var reviewer: String,
    var createdAt: Int,
    var loadKg: Double,
    var priority: Int,
    var status: StrictServiceAreaBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServiceAreaBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServiceAreaBundleStatus): Boolean {
        when (status) {
            StrictServiceAreaBundleStatus.DRAFT -> {
                when (next) {
                    StrictServiceAreaBundleStatus.ACTIVE -> return true
                }
                return false
            }
            StrictServiceAreaBundleStatus.PENDING -> return next == StrictServiceAreaBundleStatus.SUSPENDED || next == StrictServiceAreaBundleStatus.CLOSED
            StrictServiceAreaBundleStatus.ACTIVE -> return next == StrictServiceAreaBundleStatus.SUSPENDED
            StrictServiceAreaBundleStatus.SUSPENDED -> {
                when (next) {
                    StrictServiceAreaBundleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictServiceAreaBundleStatus) {
        if (!canMoveTo(next)) {
            error("strict service area bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round(createdAt.toDouble() * loadKg * 100) / 100

    fun snapshot(): StrictServiceAreaBundle {
        return StrictServiceAreaBundle(
            code = code,
            name = name,
            reviewer = reviewer,
            createdAt = createdAt,
            loadKg = loadKg,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict service area bundle in status draft. */
fun newStrictServiceAreaBundle(code: String, name: String): StrictServiceAreaBundle {
    return StrictServiceAreaBundle(
        code = code,
        name = name,
        reviewer = "",
        createdAt = 0,
        loadKg = 0.0,
        priority = 0,
        status = StrictServiceAreaBundleStatus.DRAFT,
    )
}
