package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional backhaul baseline is in its life cycle. */
enum class RegionalBackhaulBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a regional backhaul baseline and its current status. */
data class RegionalBackhaulBaseline(
    var code: String,
    var title: String,
    var shiftCode: String,
    var batchSize: Int,
    var attempts: Int,
    var discount: Double,
    var maximum: Double,
    var windowDays: Int,
    var expiresAt: Int,
    var vehicleId: String,
    var status: RegionalBackhaulBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBackhaulBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBackhaulBaselineStatus): Boolean {
        when (status) {
            RegionalBackhaulBaselineStatus.OPEN -> {
                when (next) {
                    RegionalBackhaulBaselineStatus.RESOLVED -> return true
                }
                return false
            }
            RegionalBackhaulBaselineStatus.IN_REVIEW -> return next == RegionalBackhaulBaselineStatus.REOPENED
            RegionalBackhaulBaselineStatus.RESOLVED -> return next == RegionalBackhaulBaselineStatus.REOPENED || next == RegionalBackhaulBaselineStatus.ARCHIVED
            RegionalBackhaulBaselineStatus.REOPENED -> return next == RegionalBackhaulBaselineStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalBackhaulBaselineStatus) {
        if (!canMoveTo(next)) {
            error("regional backhaul baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"regional backhaul baseline"} $code"

    fun netTotal(): Double = round((maximum - attempts.toDouble()) * 100) / 100

    fun snapshot(): RegionalBackhaulBaseline {
        return RegionalBackhaulBaseline(
            code = code,
            title = title,
            shiftCode = shiftCode,
            batchSize = batchSize,
            attempts = attempts,
            discount = discount,
            maximum = maximum,
            windowDays = windowDays,
            expiresAt = expiresAt,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional backhaul baseline in status open. */
fun newRegionalBackhaulBaseline(code: String, title: String): RegionalBackhaulBaseline {
    return RegionalBackhaulBaseline(
        code = code,
        title = title,
        shiftCode = "",
        batchSize = 0,
        attempts = 0,
        discount = 0.0,
        maximum = 0.0,
        windowDays = 0,
        expiresAt = 0,
        vehicleId = "",
        status = RegionalBackhaulBaselineStatus.OPEN,
    )
}
