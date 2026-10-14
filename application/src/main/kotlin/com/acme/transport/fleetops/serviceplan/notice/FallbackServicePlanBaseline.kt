package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback service plan baseline is in its life cycle. */
enum class FallbackServicePlanBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback service plan baseline. Its code is unique within a tenant. */
data class FallbackServicePlanBaseline(
    var code: String,
    var name: String,
    var priority: Int,
    var channel: String,
    var createdAt: Int,
    var externalRef: String,
    var sequence: Int,
    var windowDays: Int,
    var shiftCode: String,
    var checksum: String,
    var status: FallbackServicePlanBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServicePlanBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServicePlanBaselineStatus): Boolean {
        when (status) {
            FallbackServicePlanBaselineStatus.REQUESTED -> {
                when (next) {
                    FallbackServicePlanBaselineStatus.GRANTED, FallbackServicePlanBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackServicePlanBaselineStatus.GRANTED -> return next == FallbackServicePlanBaselineStatus.REVOKED || next == FallbackServicePlanBaselineStatus.EXPIRED
            FallbackServicePlanBaselineStatus.REVOKED -> {
                when (next) {
                    FallbackServicePlanBaselineStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackServicePlanBaselineStatus) {
        if (!canMoveTo(next)) {
            error("fallback service plan baseline $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((createdAt.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): FallbackServicePlanBaseline {
        return FallbackServicePlanBaseline(
            code = code,
            name = name,
            priority = priority,
            channel = channel,
            createdAt = createdAt,
            externalRef = externalRef,
            sequence = sequence,
            windowDays = windowDays,
            shiftCode = shiftCode,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback service plan baseline in status requested. */
fun newFallbackServicePlanBaseline(code: String, name: String): FallbackServicePlanBaseline {
    return FallbackServicePlanBaseline(
        code = code,
        name = name,
        priority = 0,
        channel = "",
        createdAt = 0,
        externalRef = "",
        sequence = 0,
        windowDays = 0,
        shiftCode = "",
        checksum = "",
        status = FallbackServicePlanBaselineStatus.REQUESTED,
    )
}
