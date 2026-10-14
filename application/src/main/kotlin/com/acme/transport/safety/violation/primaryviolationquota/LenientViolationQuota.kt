package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient violation quota records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a lenient violation quota is in its life cycle. */
enum class LenientViolationQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient violation quota. Its code is unique within a tenant. */
data class LenientViolationQuota(
    var code: String,
    var title: String,
    var rate: Double,
    var carrier: String,
    var tags: MutableList<String>,
    var attempts: Int,
    var depot: String,
    var minimum: Double,
    var retries: Int,
    var status: LenientViolationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientViolationQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientViolationQuotaStatus): Boolean {
        when (status) {
            LenientViolationQuotaStatus.NEW -> return next == LenientViolationQuotaStatus.QUEUED || next == LenientViolationQuotaStatus.FAILED
            LenientViolationQuotaStatus.QUEUED -> {
                when (next) {
                    LenientViolationQuotaStatus.RUNNING, LenientViolationQuotaStatus.DONE -> return true
                }
                return false
            }
            LenientViolationQuotaStatus.RUNNING -> {
                when (next) {
                    LenientViolationQuotaStatus.DONE, LenientViolationQuotaStatus.FAILED -> return true
                }
                return false
            }
            LenientViolationQuotaStatus.DONE -> return next == LenientViolationQuotaStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientViolationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("lenient violation quota $code cannot move from $status to $next")
        }
        status = next
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

    fun netTotal(): Double = round((retries.toDouble() + rate) * 100) / 100

    fun snapshot(): LenientViolationQuota {
        return LenientViolationQuota(
            code = code,
            title = title,
            rate = rate,
            carrier = carrier,
            tags = tags,
            attempts = attempts,
            depot = depot,
            minimum = minimum,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient violation quota in status new. */
fun newLenientViolationQuota(code: String, title: String): LenientViolationQuota {
    return LenientViolationQuota(
        code = code,
        title = title,
        rate = 0.0,
        carrier = "",
        tags = mutableListOf<String>(),
        attempts = 0,
        depot = "",
        minimum = 0.0,
        retries = 0,
        status = LenientViolationQuotaStatus.NEW,
    )
}
