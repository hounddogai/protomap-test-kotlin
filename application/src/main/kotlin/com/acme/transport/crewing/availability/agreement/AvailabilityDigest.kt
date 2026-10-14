package com.acme.transport.crewing.availability.agreement

import com.acme.transport.kit.text.normalize

/** Where a availability digest is in its life cycle. */
enum class AvailabilityDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One availability digest, with what the planners need to act on it. */
data class AvailabilityDigest(
    var code: String,
    var label: String,
    var dock: String,
    var active: Boolean,
    var expiresAt: Int,
    var source: String,
    var status: AvailabilityDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityDigestStatus): Boolean {
        when (status) {
            AvailabilityDigestStatus.DRAFT -> return next == AvailabilityDigestStatus.SUSPENDED
            AvailabilityDigestStatus.PENDING -> {
                when (next) {
                    AvailabilityDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            AvailabilityDigestStatus.ACTIVE -> {
                when (next) {
                    AvailabilityDigestStatus.CLOSED -> return true
                }
                return false
            }
            AvailabilityDigestStatus.SUSPENDED -> {
                when (next) {
                    AvailabilityDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityDigestStatus) {
        if (!canMoveTo(next)) {
            error("availability digest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): AvailabilityDigest {
        return AvailabilityDigest(
            code = code,
            label = label,
            dock = dock,
            active = active,
            expiresAt = expiresAt,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a availability digest in status draft. */
fun newAvailabilityDigest(code: String, label: String): AvailabilityDigest {
    return AvailabilityDigest(
        code = code,
        label = label,
        dock = "",
        active = false,
        expiresAt = 0,
        source = "",
        status = AvailabilityDigestStatus.DRAFT,
    )
}
