package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default utilization preference is in its life cycle. */
enum class DefaultUtilizationPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default utilization preference. Its code is unique within a tenant. */
data class DefaultUtilizationPreference(
    var code: String,
    var name: String,
    var description: String,
    var expiresAt: Int,
    var source: String,
    var lane: String,
    var discount: Double,
    var graceDays: Int,
    var status: DefaultUtilizationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultUtilizationPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultUtilizationPreferenceStatus): Boolean {
        when (status) {
            DefaultUtilizationPreferenceStatus.NEW -> {
                when (next) {
                    DefaultUtilizationPreferenceStatus.DONE -> return true
                }
                return false
            }
            DefaultUtilizationPreferenceStatus.QUEUED -> return next == DefaultUtilizationPreferenceStatus.FAILED
            DefaultUtilizationPreferenceStatus.RUNNING -> return next == DefaultUtilizationPreferenceStatus.DONE || next == DefaultUtilizationPreferenceStatus.FAILED
            DefaultUtilizationPreferenceStatus.DONE -> {
                when (next) {
                    DefaultUtilizationPreferenceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultUtilizationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("default utilization preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round((graceDays.toDouble() - discount) * 100) / 100

    fun snapshot(): DefaultUtilizationPreference {
        return DefaultUtilizationPreference(
            code = code,
            name = name,
            description = description,
            expiresAt = expiresAt,
            source = source,
            lane = lane,
            discount = discount,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default utilization preference in status new. */
fun newDefaultUtilizationPreference(code: String, name: String): DefaultUtilizationPreference {
    return DefaultUtilizationPreference(
        code = code,
        name = name,
        description = "",
        expiresAt = 0,
        source = "",
        lane = "",
        discount = 0.0,
        graceDays = 0,
        status = DefaultUtilizationPreferenceStatus.NEW,
    )
}
