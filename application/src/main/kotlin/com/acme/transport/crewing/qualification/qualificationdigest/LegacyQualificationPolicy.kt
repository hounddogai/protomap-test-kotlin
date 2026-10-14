package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.normalize

/** Where a legacy qualification policy is in its life cycle. */
enum class LegacyQualificationPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy qualification policy. Its code is unique within a tenant. */
data class LegacyQualificationPolicy(
    var code: String,
    var name: String,
    var carrier: String,
    var visible: Boolean,
    var category: String,
    var priority: Int,
    var status: LegacyQualificationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyQualificationPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyQualificationPolicyStatus): Boolean {
        when (status) {
            LegacyQualificationPolicyStatus.REQUESTED -> {
                when (next) {
                    LegacyQualificationPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyQualificationPolicyStatus.GRANTED -> return next == LegacyQualificationPolicyStatus.REVOKED || next == LegacyQualificationPolicyStatus.EXPIRED
            LegacyQualificationPolicyStatus.REVOKED -> return next == LegacyQualificationPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyQualificationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("legacy qualification policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): LegacyQualificationPolicy {
        return LegacyQualificationPolicy(
            code = code,
            name = name,
            carrier = carrier,
            visible = visible,
            category = category,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy qualification policy in status requested. */
fun newLegacyQualificationPolicy(code: String, name: String): LegacyQualificationPolicy {
    return LegacyQualificationPolicy(
        code = code,
        name = name,
        carrier = "",
        visible = false,
        category = "",
        priority = 0,
        status = LegacyQualificationPolicyStatus.REQUESTED,
    )
}
