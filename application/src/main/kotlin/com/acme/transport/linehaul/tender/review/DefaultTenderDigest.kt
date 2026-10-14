package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tender digest is in its life cycle. */
enum class DefaultTenderDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One default tender digest, with what the planners need to act on it. */
data class DefaultTenderDigest(
    var code: String,
    var name: String,
    var odometer: Int,
    var threshold: Double,
    var segment: String,
    var summary: String,
    var contactEmail: String,
    var createdAt: Int,
    var lane: String,
    var status: DefaultTenderDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTenderDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTenderDigestStatus): Boolean {
        when (status) {
            DefaultTenderDigestStatus.PROPOSED -> return next == DefaultTenderDigestStatus.APPROVED || next == DefaultTenderDigestStatus.REJECTED
            DefaultTenderDigestStatus.APPROVED -> return next == DefaultTenderDigestStatus.REJECTED || next == DefaultTenderDigestStatus.WITHDRAWN
            DefaultTenderDigestStatus.REJECTED -> return next == DefaultTenderDigestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultTenderDigestStatus) {
        if (!canMoveTo(next)) {
            error("default tender digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default tender digest"} $code"

    fun netExposure(): Double = round((threshold - createdAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultTenderDigest {
        return DefaultTenderDigest(
            code = code,
            name = name,
            odometer = odometer,
            threshold = threshold,
            segment = segment,
            summary = summary,
            contactEmail = contactEmail,
            createdAt = createdAt,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default tender digest in status proposed. */
fun newDefaultTenderDigest(code: String, name: String): DefaultTenderDigest {
    return DefaultTenderDigest(
        code = code,
        name = name,
        odometer = 0,
        threshold = 0.0,
        segment = "",
        summary = "",
        contactEmail = "",
        createdAt = 0,
        lane = "",
        status = DefaultTenderDigestStatus.PROPOSED,
    )
}
