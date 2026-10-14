package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual audit policy records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a manual audit policy is in its life cycle. */
enum class ManualAuditPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A manual audit policy. Its code is unique within a tenant. */
data class ManualAuditPolicy(
    var code: String,
    var title: String,
    var pallets: Int,
    var version: Int,
    var trailerId: String,
    var carrier: String,
    var phone: String,
    var ratio: Double,
    var threshold: Double,
    var weight: Double,
    var status: ManualAuditPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAuditPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAuditPolicyStatus): Boolean {
        when (status) {
            ManualAuditPolicyStatus.OPEN -> return next == ManualAuditPolicyStatus.ARCHIVED
            ManualAuditPolicyStatus.IN_REVIEW -> {
                when (next) {
                    ManualAuditPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualAuditPolicyStatus.RESOLVED -> return next == ManualAuditPolicyStatus.REOPENED || next == ManualAuditPolicyStatus.ARCHIVED
            ManualAuditPolicyStatus.REOPENED -> return next == ManualAuditPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualAuditPolicyStatus) {
        if (!canMoveTo(next)) {
            error("manual audit policy $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedLoad(): Double = round((pallets.toDouble() - threshold) * 100) / 100

    fun snapshot(): ManualAuditPolicy {
        return ManualAuditPolicy(
            code = code,
            title = title,
            pallets = pallets,
            version = version,
            trailerId = trailerId,
            carrier = carrier,
            phone = phone,
            ratio = ratio,
            threshold = threshold,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual audit policy in status open. */
fun newManualAuditPolicy(code: String, title: String): ManualAuditPolicy {
    return ManualAuditPolicy(
        code = code,
        title = title,
        pallets = 0,
        version = 0,
        trailerId = "",
        carrier = "",
        phone = "",
        ratio = 0.0,
        threshold = 0.0,
        weight = 0.0,
        status = ManualAuditPolicyStatus.OPEN,
    )
}
