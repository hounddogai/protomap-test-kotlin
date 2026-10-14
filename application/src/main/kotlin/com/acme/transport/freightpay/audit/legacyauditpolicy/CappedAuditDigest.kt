package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped audit digest is in its life cycle. */
enum class CappedAuditDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped audit digest, with what the planners need to act on it. */
data class CappedAuditDigest(
    var code: String,
    var name: String,
    var revision: Int,
    var cost: Double,
    var score: Double,
    var minimum: Double,
    var reviewer: String,
    var weight: Double,
    var status: CappedAuditDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAuditDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAuditDigestStatus): Boolean {
        when (status) {
            CappedAuditDigestStatus.IDLE -> {
                when (next) {
                    CappedAuditDigestStatus.READY -> return true
                }
                return false
            }
            CappedAuditDigestStatus.WARMING -> {
                when (next) {
                    CappedAuditDigestStatus.DRAINING, CappedAuditDigestStatus.STOPPED -> return true
                }
                return false
            }
            CappedAuditDigestStatus.READY -> {
                when (next) {
                    CappedAuditDigestStatus.DRAINING, CappedAuditDigestStatus.STOPPED -> return true
                }
                return false
            }
            CappedAuditDigestStatus.DRAINING -> {
                when (next) {
                    CappedAuditDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAuditDigestStatus) {
        if (!canMoveTo(next)) {
            error("capped audit digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((score - minimum) * 100) / 100

    fun snapshot(): CappedAuditDigest {
        return CappedAuditDigest(
            code = code,
            name = name,
            revision = revision,
            cost = cost,
            score = score,
            minimum = minimum,
            reviewer = reviewer,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped audit digest in status idle. */
fun newCappedAuditDigest(code: String, name: String): CappedAuditDigest {
    return CappedAuditDigest(
        code = code,
        name = name,
        revision = 0,
        cost = 0.0,
        score = 0.0,
        minimum = 0.0,
        reviewer = "",
        weight = 0.0,
        status = CappedAuditDigestStatus.IDLE,
    )
}
