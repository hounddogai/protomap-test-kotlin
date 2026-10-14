package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity digest is in its life cycle. */
enum class CapacityDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One capacity digest, with what the planners need to act on it. */
data class CapacityDigest(
    var code: String,
    var title: String,
    var rate: Double,
    var visible: Boolean,
    var version: Int,
    var minimum: Double,
    var status: CapacityDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityDigestStatus): Boolean {
        when (status) {
            CapacityDigestStatus.REQUESTED -> {
                when (next) {
                    CapacityDigestStatus.REVOKED -> return true
                }
                return false
            }
            CapacityDigestStatus.GRANTED -> return next == CapacityDigestStatus.REVOKED || next == CapacityDigestStatus.EXPIRED
            CapacityDigestStatus.REVOKED -> return next == CapacityDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CapacityDigestStatus) {
        if (!canMoveTo(next)) {
            error("capacity digest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round((rate + minimum) * 100) / 100

    fun snapshot(): CapacityDigest {
        return CapacityDigest(
            code = code,
            title = title,
            rate = rate,
            visible = visible,
            version = version,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capacity digest in status requested. */
fun newCapacityDigest(code: String, title: String): CapacityDigest {
    return CapacityDigest(
        code = code,
        title = title,
        rate = 0.0,
        visible = false,
        version = 0,
        minimum = 0.0,
        status = CapacityDigestStatus.REQUESTED,
    )
}
