package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global idling rollup is in its life cycle. */
enum class GlobalIdlingRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global idling rollup and its current status. */
data class GlobalIdlingRollup(
    var code: String,
    var name: String,
    var ceiling: Double,
    var createdAt: Int,
    var revision: Int,
    var rate: Double,
    var region: String,
    var contactEmail: String,
    var checksum: String,
    var loadKg: Double,
    var distanceKm: Double,
    var status: GlobalIdlingRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalIdlingRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalIdlingRollupStatus): Boolean {
        when (status) {
            GlobalIdlingRollupStatus.PROPOSED -> {
                when (next) {
                    GlobalIdlingRollupStatus.APPROVED -> return true
                }
                return false
            }
            GlobalIdlingRollupStatus.APPROVED -> {
                when (next) {
                    GlobalIdlingRollupStatus.REJECTED, GlobalIdlingRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalIdlingRollupStatus.REJECTED -> return next == GlobalIdlingRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalIdlingRollupStatus) {
        if (!canMoveTo(next)) {
            error("global idling rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedValue(): Double = round((rate - createdAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalIdlingRollup {
        return GlobalIdlingRollup(
            code = code,
            name = name,
            ceiling = ceiling,
            createdAt = createdAt,
            revision = revision,
            rate = rate,
            region = region,
            contactEmail = contactEmail,
            checksum = checksum,
            loadKg = loadKg,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global idling rollup in status proposed. */
fun newGlobalIdlingRollup(code: String, name: String): GlobalIdlingRollup {
    return GlobalIdlingRollup(
        code = code,
        name = name,
        ceiling = 0.0,
        createdAt = 0,
        revision = 0,
        rate = 0.0,
        region = "",
        contactEmail = "",
        checksum = "",
        loadKg = 0.0,
        distanceKm = 0.0,
        status = GlobalIdlingRollupStatus.PROPOSED,
    )
}
