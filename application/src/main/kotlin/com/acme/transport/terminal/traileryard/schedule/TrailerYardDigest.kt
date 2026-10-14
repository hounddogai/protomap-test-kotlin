package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard digest is in its life cycle. */
enum class TrailerYardDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A trailer yard digest. Its code is unique within a tenant. */
data class TrailerYardDigest(
    var code: String,
    var label: String,
    var threshold: Double,
    var floorValue: Double,
    var attributes: MutableMap<String, String>,
    var status: TrailerYardDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardDigestStatus): Boolean {
        when (status) {
            TrailerYardDigestStatus.OPEN -> {
                when (next) {
                    TrailerYardDigestStatus.RESOLVED, TrailerYardDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            TrailerYardDigestStatus.IN_REVIEW -> {
                when (next) {
                    TrailerYardDigestStatus.RESOLVED, TrailerYardDigestStatus.REOPENED -> return true
                }
                return false
            }
            TrailerYardDigestStatus.RESOLVED -> return next == TrailerYardDigestStatus.REOPENED || next == TrailerYardDigestStatus.ARCHIVED
            TrailerYardDigestStatus.REOPENED -> {
                when (next) {
                    TrailerYardDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardDigestStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"trailer yard digest"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((threshold - floorValue) * 100) / 100

    fun snapshot(): TrailerYardDigest {
        return TrailerYardDigest(
            code = code,
            label = label,
            threshold = threshold,
            floorValue = floorValue,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer yard digest in status open. */
fun newTrailerYardDigest(code: String, label: String): TrailerYardDigest {
    return TrailerYardDigest(
        code = code,
        label = label,
        threshold = 0.0,
        floorValue = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = TrailerYardDigestStatus.OPEN,
    )
}
