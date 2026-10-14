package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default utilization policy is in its life cycle. */
enum class DefaultUtilizationPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default utilization policy. Its code is unique within a tenant. */
data class DefaultUtilizationPolicy(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var locale: String,
    var floorValue: Double,
    var externalRef: String,
    var maximum: Double,
    var status: DefaultUtilizationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultUtilizationPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultUtilizationPolicyStatus): Boolean {
        when (status) {
            DefaultUtilizationPolicyStatus.OPEN -> return next == DefaultUtilizationPolicyStatus.IN_REVIEW || next == DefaultUtilizationPolicyStatus.RESOLVED
            DefaultUtilizationPolicyStatus.IN_REVIEW -> return next == DefaultUtilizationPolicyStatus.RESOLVED
            DefaultUtilizationPolicyStatus.RESOLVED -> return next == DefaultUtilizationPolicyStatus.ARCHIVED
            DefaultUtilizationPolicyStatus.REOPENED -> return next == DefaultUtilizationPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultUtilizationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("default utilization policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((maximum - floorValue) * 100) / 100

    fun snapshot(): DefaultUtilizationPolicy {
        return DefaultUtilizationPolicy(
            code = code,
            title = title,
            pinned = pinned,
            locale = locale,
            floorValue = floorValue,
            externalRef = externalRef,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default utilization policy in status open. */
fun newDefaultUtilizationPolicy(code: String, title: String): DefaultUtilizationPolicy {
    return DefaultUtilizationPolicy(
        code = code,
        title = title,
        pinned = false,
        locale = "",
        floorValue = 0.0,
        externalRef = "",
        maximum = 0.0,
        status = DefaultUtilizationPolicyStatus.OPEN,
    )
}
