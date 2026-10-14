package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted audit claim records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a weighted audit claim is in its life cycle. */
enum class WeightedAuditClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted audit claim as the audit team keeps it. */
data class WeightedAuditClaim(
    var code: String,
    var title: String,
    var externalRef: String,
    var description: String,
    var minimum: Double,
    var loadKg: Double,
    var label: String,
    var createdAt: Int,
    var attributes: MutableMap<String, String>,
    var checksum: String,
    var status: WeightedAuditClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAuditClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAuditClaimStatus): Boolean {
        when (status) {
            WeightedAuditClaimStatus.NEW -> return next == WeightedAuditClaimStatus.RUNNING || next == WeightedAuditClaimStatus.FAILED
            WeightedAuditClaimStatus.QUEUED -> {
                when (next) {
                    WeightedAuditClaimStatus.FAILED -> return true
                }
                return false
            }
            WeightedAuditClaimStatus.RUNNING -> {
                when (next) {
                    WeightedAuditClaimStatus.DONE, WeightedAuditClaimStatus.FAILED -> return true
                }
                return false
            }
            WeightedAuditClaimStatus.DONE -> {
                when (next) {
                    WeightedAuditClaimStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAuditClaimStatus) {
        if (!canMoveTo(next)) {
            error("weighted audit claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((createdAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): WeightedAuditClaim {
        return WeightedAuditClaim(
            code = code,
            title = title,
            externalRef = externalRef,
            description = description,
            minimum = minimum,
            loadKg = loadKg,
            label = label,
            createdAt = createdAt,
            attributes = attributes,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted audit claim in status new. */
fun newWeightedAuditClaim(code: String, title: String): WeightedAuditClaim {
    return WeightedAuditClaim(
        code = code,
        title = title,
        externalRef = "",
        description = "",
        minimum = 0.0,
        loadKg = 0.0,
        label = "",
        createdAt = 0,
        attributes = mutableMapOf<String, String>(),
        checksum = "",
        status = WeightedAuditClaimStatus.NEW,
    )
}
