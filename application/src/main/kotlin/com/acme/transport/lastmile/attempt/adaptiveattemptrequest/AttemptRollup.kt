package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt rollup is in its life cycle. */
enum class AttemptRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A attempt rollup. Its code is unique within a tenant. */
data class AttemptRollup(
    var code: String,
    var title: String,
    var createdAt: Int,
    var timezone: String,
    var category: String,
    var checksum: String,
    var cost: Double,
    var phone: String,
    var driverId: String,
    var status: AttemptRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptRollupStatus): Boolean {
        when (status) {
            AttemptRollupStatus.DRAFT -> {
                when (next) {
                    AttemptRollupStatus.PENDING, AttemptRollupStatus.SUSPENDED -> return true
                }
                return false
            }
            AttemptRollupStatus.PENDING -> return next == AttemptRollupStatus.CLOSED
            AttemptRollupStatus.ACTIVE -> {
                when (next) {
                    AttemptRollupStatus.SUSPENDED, AttemptRollupStatus.CLOSED -> return true
                }
                return false
            }
            AttemptRollupStatus.SUSPENDED -> {
                when (next) {
                    AttemptRollupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptRollupStatus) {
        if (!canMoveTo(next)) {
            error("attempt rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round((cost - createdAt.toDouble()) * 100) / 100

    fun snapshot(): AttemptRollup {
        return AttemptRollup(
            code = code,
            title = title,
            createdAt = createdAt,
            timezone = timezone,
            category = category,
            checksum = checksum,
            cost = cost,
            phone = phone,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a attempt rollup in status draft. */
fun newAttemptRollup(code: String, title: String): AttemptRollup {
    return AttemptRollup(
        code = code,
        title = title,
        createdAt = 0,
        timezone = "",
        category = "",
        checksum = "",
        cost = 0.0,
        phone = "",
        driverId = "",
        status = AttemptRollupStatus.DRAFT,
    )
}
