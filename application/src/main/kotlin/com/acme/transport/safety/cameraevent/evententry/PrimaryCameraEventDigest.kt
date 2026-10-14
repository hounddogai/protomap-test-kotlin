package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary camera event digest is in its life cycle. */
enum class PrimaryCameraEventDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a primary camera event digest and its current status. */
data class PrimaryCameraEventDigest(
    var code: String,
    var title: String,
    var amount: Double,
    var locale: String,
    var startsAt: Int,
    var externalRef: String,
    var sequence: Int,
    var status: PrimaryCameraEventDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCameraEventDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCameraEventDigestStatus): Boolean {
        when (status) {
            PrimaryCameraEventDigestStatus.NEW -> {
                when (next) {
                    PrimaryCameraEventDigestStatus.QUEUED -> return true
                }
                return false
            }
            PrimaryCameraEventDigestStatus.QUEUED -> {
                when (next) {
                    PrimaryCameraEventDigestStatus.RUNNING, PrimaryCameraEventDigestStatus.DONE -> return true
                }
                return false
            }
            PrimaryCameraEventDigestStatus.RUNNING -> return next == PrimaryCameraEventDigestStatus.FAILED
            PrimaryCameraEventDigestStatus.DONE -> return next == PrimaryCameraEventDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryCameraEventDigestStatus) {
        if (!canMoveTo(next)) {
            error("primary camera event digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary camera event digest"} $code"

    fun projectedExposure(): Double = round((startsAt.toDouble() + amount) * 100) / 100

    fun snapshot(): PrimaryCameraEventDigest {
        return PrimaryCameraEventDigest(
            code = code,
            title = title,
            amount = amount,
            locale = locale,
            startsAt = startsAt,
            externalRef = externalRef,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary camera event digest in status new. */
fun newPrimaryCameraEventDigest(code: String, title: String): PrimaryCameraEventDigest {
    return PrimaryCameraEventDigest(
        code = code,
        title = title,
        amount = 0.0,
        locale = "",
        startsAt = 0,
        externalRef = "",
        sequence = 0,
        status = PrimaryCameraEventDigestStatus.NEW,
    )
}
