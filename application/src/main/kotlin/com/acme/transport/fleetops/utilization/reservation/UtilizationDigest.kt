package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.text.normalize

/** Where a utilization digest is in its life cycle. */
enum class UtilizationDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One utilization digest, with what the planners need to act on it. */
data class UtilizationDigest(
    var code: String,
    var title: String,
    var locale: String,
    var region: String,
    var source: String,
    var trailerId: String,
    var summary: String,
    var amount: Double,
    var status: UtilizationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationDigestStatus): Boolean {
        when (status) {
            UtilizationDigestStatus.NEW -> {
                when (next) {
                    UtilizationDigestStatus.DONE -> return true
                }
                return false
            }
            UtilizationDigestStatus.QUEUED -> return next == UtilizationDigestStatus.RUNNING || next == UtilizationDigestStatus.DONE
            UtilizationDigestStatus.RUNNING -> return next == UtilizationDigestStatus.FAILED
            UtilizationDigestStatus.DONE -> {
                when (next) {
                    UtilizationDigestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationDigestStatus) {
        if (!canMoveTo(next)) {
            error("utilization digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = amount

    fun snapshot(): UtilizationDigest {
        return UtilizationDigest(
            code = code,
            title = title,
            locale = locale,
            region = region,
            source = source,
            trailerId = trailerId,
            summary = summary,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a utilization digest in status new. */
fun newUtilizationDigest(code: String, title: String): UtilizationDigest {
    return UtilizationDigest(
        code = code,
        title = title,
        locale = "",
        region = "",
        source = "",
        trailerId = "",
        summary = "",
        amount = 0.0,
        status = UtilizationDigestStatus.NEW,
    )
}
