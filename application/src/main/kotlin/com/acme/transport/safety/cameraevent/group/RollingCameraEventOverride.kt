package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling camera event override is in its life cycle. */
enum class RollingCameraEventOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rolling camera event override and its current status. */
data class RollingCameraEventOverride(
    var code: String,
    var label: String,
    var region: String,
    var retries: Int,
    var routeCode: String,
    var windowDays: Int,
    var checksum: String,
    var margin: Double,
    var segment: String,
    var name: String,
    var status: RollingCameraEventOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCameraEventOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCameraEventOverrideStatus): Boolean {
        when (status) {
            RollingCameraEventOverrideStatus.REQUESTED -> return next == RollingCameraEventOverrideStatus.EXPIRED
            RollingCameraEventOverrideStatus.GRANTED -> return next == RollingCameraEventOverrideStatus.REVOKED || next == RollingCameraEventOverrideStatus.EXPIRED
            RollingCameraEventOverrideStatus.REVOKED -> return next == RollingCameraEventOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingCameraEventOverrideStatus) {
        if (!canMoveTo(next)) {
            error("rolling camera event override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round((retries.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): RollingCameraEventOverride {
        return RollingCameraEventOverride(
            code = code,
            label = label,
            region = region,
            retries = retries,
            routeCode = routeCode,
            windowDays = windowDays,
            checksum = checksum,
            margin = margin,
            segment = segment,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling camera event override in status requested. */
fun newRollingCameraEventOverride(code: String, label: String): RollingCameraEventOverride {
    return RollingCameraEventOverride(
        code = code,
        label = label,
        region = "",
        retries = 0,
        routeCode = "",
        windowDays = 0,
        checksum = "",
        margin = 0.0,
        segment = "",
        name = "",
        status = RollingCameraEventOverrideStatus.REQUESTED,
    )
}
