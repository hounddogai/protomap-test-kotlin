package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul override is in its life cycle. */
enum class BackhaulOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A backhaul override. Its code is unique within a tenant. */
data class BackhaulOverride(
    var code: String,
    var name: String,
    var attempts: Int,
    var unit: String,
    var createdAt: Int,
    var status: BackhaulOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulOverrideStatus): Boolean {
        when (status) {
            BackhaulOverrideStatus.CREATED -> return next == BackhaulOverrideStatus.VALIDATED || next == BackhaulOverrideStatus.PUBLISHED
            BackhaulOverrideStatus.VALIDATED -> {
                when (next) {
                    BackhaulOverrideStatus.PUBLISHED, BackhaulOverrideStatus.RETIRED -> return true
                }
                return false
            }
            BackhaulOverrideStatus.PUBLISHED -> {
                when (next) {
                    BackhaulOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulOverrideStatus) {
        if (!canMoveTo(next)) {
            error("backhaul override $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((attempts.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): BackhaulOverride {
        return BackhaulOverride(
            code = code,
            name = name,
            attempts = attempts,
            unit = unit,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a backhaul override in status created. */
fun newBackhaulOverride(code: String, name: String): BackhaulOverride {
    return BackhaulOverride(
        code = code,
        name = name,
        attempts = 0,
        unit = "",
        createdAt = 0,
        status = BackhaulOverrideStatus.CREATED,
    )
}
