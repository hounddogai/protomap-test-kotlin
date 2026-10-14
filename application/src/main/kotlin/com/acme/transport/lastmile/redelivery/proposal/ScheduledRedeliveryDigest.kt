package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled redelivery digest is in its life cycle. */
enum class ScheduledRedeliveryDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scheduled redelivery digest, with what the planners need to act on it. */
data class ScheduledRedeliveryDigest(
    var code: String,
    var label: String,
    var version: Int,
    var limit: Int,
    var timezone: String,
    var externalRef: String,
    var dock: String,
    var expiresAt: Int,
    var locale: String,
    var graceDays: Int,
    var status: ScheduledRedeliveryDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRedeliveryDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRedeliveryDigestStatus): Boolean {
        when (status) {
            ScheduledRedeliveryDigestStatus.DRAFT -> {
                when (next) {
                    ScheduledRedeliveryDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledRedeliveryDigestStatus.PENDING -> {
                when (next) {
                    ScheduledRedeliveryDigestStatus.SUSPENDED, ScheduledRedeliveryDigestStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledRedeliveryDigestStatus.ACTIVE -> {
                when (next) {
                    ScheduledRedeliveryDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledRedeliveryDigestStatus.SUSPENDED -> return next == ScheduledRedeliveryDigestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledRedeliveryDigestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled redelivery digest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round(version.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledRedeliveryDigest {
        return ScheduledRedeliveryDigest(
            code = code,
            label = label,
            version = version,
            limit = limit,
            timezone = timezone,
            externalRef = externalRef,
            dock = dock,
            expiresAt = expiresAt,
            locale = locale,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled redelivery digest in status draft. */
fun newScheduledRedeliveryDigest(code: String, label: String): ScheduledRedeliveryDigest {
    return ScheduledRedeliveryDigest(
        code = code,
        label = label,
        version = 0,
        limit = 0,
        timezone = "",
        externalRef = "",
        dock = "",
        expiresAt = 0,
        locale = "",
        graceDays = 0,
        status = ScheduledRedeliveryDigestStatus.DRAFT,
    )
}
