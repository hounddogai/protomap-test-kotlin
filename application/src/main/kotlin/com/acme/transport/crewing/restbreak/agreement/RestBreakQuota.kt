package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.text.normalize

/** Where a rest break quota is in its life cycle. */
enum class RestBreakQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rest break quota, with what the planners need to act on it. */
data class RestBreakQuota(
    var code: String,
    var label: String,
    var batchSize: Int,
    var pinned: Boolean,
    var channel: String,
    var status: RestBreakQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakQuotaStatus): Boolean {
        when (status) {
            RestBreakQuotaStatus.NEW -> return next == RestBreakQuotaStatus.RUNNING
            RestBreakQuotaStatus.QUEUED -> {
                when (next) {
                    RestBreakQuotaStatus.FAILED -> return true
                }
                return false
            }
            RestBreakQuotaStatus.RUNNING -> return next == RestBreakQuotaStatus.DONE
            RestBreakQuotaStatus.DONE -> {
                when (next) {
                    RestBreakQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rest break quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): RestBreakQuota {
        return RestBreakQuota(
            code = code,
            label = label,
            batchSize = batchSize,
            pinned = pinned,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rest break quota in status new. */
fun newRestBreakQuota(code: String, label: String): RestBreakQuota {
    return RestBreakQuota(
        code = code,
        label = label,
        batchSize = 0,
        pinned = false,
        channel = "",
        status = RestBreakQuotaStatus.NEW,
    )
}
