package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling qualification digest records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a rolling qualification digest is in its life cycle. */
enum class RollingQualificationDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rolling qualification digest, with what the planners need to act on it. */
data class RollingQualificationDigest(
    var code: String,
    var name: String,
    var retries: Int,
    var capacity: Int,
    var rate: Double,
    var pallets: Int,
    var tags: MutableList<String>,
    var status: RollingQualificationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingQualificationDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingQualificationDigestStatus): Boolean {
        when (status) {
            RollingQualificationDigestStatus.NEW -> return next == RollingQualificationDigestStatus.FAILED
            RollingQualificationDigestStatus.QUEUED -> return next == RollingQualificationDigestStatus.FAILED
            RollingQualificationDigestStatus.RUNNING -> return next == RollingQualificationDigestStatus.FAILED
            RollingQualificationDigestStatus.DONE -> return next == RollingQualificationDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingQualificationDigestStatus) {
        if (!canMoveTo(next)) {
            error("rolling qualification digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round(capacity.toDouble() * rate * 100) / 100

    fun snapshot(): RollingQualificationDigest {
        return RollingQualificationDigest(
            code = code,
            name = name,
            retries = retries,
            capacity = capacity,
            rate = rate,
            pallets = pallets,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling qualification digest in status new. */
fun newRollingQualificationDigest(code: String, name: String): RollingQualificationDigest {
    return RollingQualificationDigest(
        code = code,
        name = name,
        retries = 0,
        capacity = 0,
        rate = 0.0,
        pallets = 0,
        tags = mutableListOf<String>(),
        status = RollingQualificationDigestStatus.NEW,
    )
}
