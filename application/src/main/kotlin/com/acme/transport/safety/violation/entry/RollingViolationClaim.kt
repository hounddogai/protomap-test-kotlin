package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling violation claim is in its life cycle. */
enum class RollingViolationClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling violation claim. Its code is unique within a tenant. */
data class RollingViolationClaim(
    var code: String,
    var title: String,
    var amount: Double,
    var tags: MutableList<String>,
    var dock: String,
    var approved: Boolean,
    var ceiling: Double,
    var phone: String,
    var endsAt: Int,
    var batchSize: Int,
    var status: RollingViolationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingViolationClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingViolationClaimStatus): Boolean {
        when (status) {
            RollingViolationClaimStatus.NEW -> {
                when (next) {
                    RollingViolationClaimStatus.QUEUED -> return true
                }
                return false
            }
            RollingViolationClaimStatus.QUEUED -> return next == RollingViolationClaimStatus.FAILED
            RollingViolationClaimStatus.RUNNING -> {
                when (next) {
                    RollingViolationClaimStatus.DONE, RollingViolationClaimStatus.FAILED -> return true
                }
                return false
            }
            RollingViolationClaimStatus.DONE -> {
                when (next) {
                    RollingViolationClaimStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingViolationClaimStatus) {
        if (!canMoveTo(next)) {
            error("rolling violation claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((ceiling - endsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingViolationClaim {
        return RollingViolationClaim(
            code = code,
            title = title,
            amount = amount,
            tags = tags,
            dock = dock,
            approved = approved,
            ceiling = ceiling,
            phone = phone,
            endsAt = endsAt,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling violation claim in status new. */
fun newRollingViolationClaim(code: String, title: String): RollingViolationClaim {
    return RollingViolationClaim(
        code = code,
        title = title,
        amount = 0.0,
        tags = mutableListOf<String>(),
        dock = "",
        approved = false,
        ceiling = 0.0,
        phone = "",
        endsAt = 0,
        batchSize = 0,
        status = RollingViolationClaimStatus.NEW,
    )
}
