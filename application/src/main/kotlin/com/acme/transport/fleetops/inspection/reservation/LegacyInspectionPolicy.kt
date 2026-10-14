package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection policy is in its life cycle. */
enum class LegacyInspectionPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy inspection policy. Its code is unique within a tenant. */
data class LegacyInspectionPolicy(
    var code: String,
    var name: String,
    var category: String,
    var durationMinutes: Int,
    var endsAt: Int,
    var discount: Double,
    var region: String,
    var source: String,
    var retries: Int,
    var phone: String,
    var priority: Int,
    var status: LegacyInspectionPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionPolicyStatus): Boolean {
        when (status) {
            LegacyInspectionPolicyStatus.PROPOSED -> {
                when (next) {
                    LegacyInspectionPolicyStatus.APPROVED, LegacyInspectionPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyInspectionPolicyStatus.APPROVED -> {
                when (next) {
                    LegacyInspectionPolicyStatus.REJECTED, LegacyInspectionPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyInspectionPolicyStatus.REJECTED -> {
                when (next) {
                    LegacyInspectionPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyInspectionPolicyStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((durationMinutes.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): LegacyInspectionPolicy {
        return LegacyInspectionPolicy(
            code = code,
            name = name,
            category = category,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            discount = discount,
            region = region,
            source = source,
            retries = retries,
            phone = phone,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy inspection policy in status proposed. */
fun newLegacyInspectionPolicy(code: String, name: String): LegacyInspectionPolicy {
    return LegacyInspectionPolicy(
        code = code,
        name = name,
        category = "",
        durationMinutes = 0,
        endsAt = 0,
        discount = 0.0,
        region = "",
        source = "",
        retries = 0,
        phone = "",
        priority = 0,
        status = LegacyInspectionPolicyStatus.PROPOSED,
    )
}
