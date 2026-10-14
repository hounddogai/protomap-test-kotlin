package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default backhaul digest is in its life cycle. */
enum class DefaultBackhaulDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default backhaul digest as the backhaul team keeps it. */
data class DefaultBackhaulDigest(
    var code: String,
    var name: String,
    var amount: Double,
    var startsAt: Int,
    var timezone: String,
    var status: DefaultBackhaulDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultBackhaulDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultBackhaulDigestStatus): Boolean {
        when (status) {
            DefaultBackhaulDigestStatus.DRAFT -> {
                when (next) {
                    DefaultBackhaulDigestStatus.PENDING, DefaultBackhaulDigestStatus.CLOSED -> return true
                }
                return false
            }
            DefaultBackhaulDigestStatus.PENDING -> {
                when (next) {
                    DefaultBackhaulDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultBackhaulDigestStatus.ACTIVE -> return next == DefaultBackhaulDigestStatus.CLOSED
            DefaultBackhaulDigestStatus.SUSPENDED -> {
                when (next) {
                    DefaultBackhaulDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultBackhaulDigestStatus) {
        if (!canMoveTo(next)) {
            error("default backhaul digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default backhaul digest"} $code"

    fun adjustedValue(): Double = round((amount + startsAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultBackhaulDigest {
        return DefaultBackhaulDigest(
            code = code,
            name = name,
            amount = amount,
            startsAt = startsAt,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default backhaul digest in status draft. */
fun newDefaultBackhaulDigest(code: String, name: String): DefaultBackhaulDigest {
    return DefaultBackhaulDigest(
        code = code,
        name = name,
        amount = 0.0,
        startsAt = 0,
        timezone = "",
        status = DefaultBackhaulDigestStatus.DRAFT,
    )
}
