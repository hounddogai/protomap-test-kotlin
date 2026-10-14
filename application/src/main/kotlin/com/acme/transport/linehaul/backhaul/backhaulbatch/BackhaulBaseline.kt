package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul baseline is in its life cycle. */
enum class BackhaulBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a backhaul baseline and its current status. */
data class BackhaulBaseline(
    var code: String,
    var label: String,
    var shiftCode: String,
    var reviewer: String,
    var minimum: Double,
    var country: String,
    var fuelLiters: Double,
    var revision: Int,
    var createdAt: Int,
    var ceiling: Double,
    var approved: Boolean,
    var status: BackhaulBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulBaselineStatus): Boolean {
        when (status) {
            BackhaulBaselineStatus.IDLE -> {
                when (next) {
                    BackhaulBaselineStatus.WARMING -> return true
                }
                return false
            }
            BackhaulBaselineStatus.WARMING -> return next == BackhaulBaselineStatus.DRAINING || next == BackhaulBaselineStatus.STOPPED
            BackhaulBaselineStatus.READY -> {
                when (next) {
                    BackhaulBaselineStatus.DRAINING -> return true
                }
                return false
            }
            BackhaulBaselineStatus.DRAINING -> {
                when (next) {
                    BackhaulBaselineStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulBaselineStatus) {
        if (!canMoveTo(next)) {
            error("backhaul baseline $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round(createdAt.toDouble() * minimum * 100) / 100

    fun snapshot(): BackhaulBaseline {
        return BackhaulBaseline(
            code = code,
            label = label,
            shiftCode = shiftCode,
            reviewer = reviewer,
            minimum = minimum,
            country = country,
            fuelLiters = fuelLiters,
            revision = revision,
            createdAt = createdAt,
            ceiling = ceiling,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a backhaul baseline in status idle. */
fun newBackhaulBaseline(code: String, label: String): BackhaulBaseline {
    return BackhaulBaseline(
        code = code,
        label = label,
        shiftCode = "",
        reviewer = "",
        minimum = 0.0,
        country = "",
        fuelLiters = 0.0,
        revision = 0,
        createdAt = 0,
        ceiling = 0.0,
        approved = false,
        status = BackhaulBaselineStatus.IDLE,
    )
}
