package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy redelivery policy is in its life cycle. */
enum class LegacyRedeliveryPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy redelivery policy. Its code is unique within a tenant. */
data class LegacyRedeliveryPolicy(
    var code: String,
    var label: String,
    var graceDays: Int,
    var threshold: Double,
    var distanceKm: Double,
    var category: String,
    var status: LegacyRedeliveryPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRedeliveryPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRedeliveryPolicyStatus): Boolean {
        when (status) {
            LegacyRedeliveryPolicyStatus.PLANNED -> {
                when (next) {
                    LegacyRedeliveryPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyRedeliveryPolicyStatus.SCHEDULED -> {
                when (next) {
                    LegacyRedeliveryPolicyStatus.IN_PROGRESS, LegacyRedeliveryPolicyStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyRedeliveryPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyRedeliveryPolicyStatus.COMPLETED, LegacyRedeliveryPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyRedeliveryPolicyStatus.COMPLETED -> {
                when (next) {
                    LegacyRedeliveryPolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRedeliveryPolicyStatus) {
        if (!canMoveTo(next)) {
            error("legacy redelivery policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy redelivery policy"} $code"

    fun weightedExposure(): Double = round((graceDays.toDouble() + threshold) * 100) / 100

    fun snapshot(): LegacyRedeliveryPolicy {
        return LegacyRedeliveryPolicy(
            code = code,
            label = label,
            graceDays = graceDays,
            threshold = threshold,
            distanceKm = distanceKm,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy redelivery policy in status planned. */
fun newLegacyRedeliveryPolicy(code: String, label: String): LegacyRedeliveryPolicy {
    return LegacyRedeliveryPolicy(
        code = code,
        label = label,
        graceDays = 0,
        threshold = 0.0,
        distanceKm = 0.0,
        category = "",
        status = LegacyRedeliveryPolicyStatus.PLANNED,
    )
}
