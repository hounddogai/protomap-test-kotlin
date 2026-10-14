package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.text.normalize

// The attempt digest records of lastmile's attempt work, and the rules, stores, and calculations around them.

/** Where a attempt digest is in its life cycle. */
enum class AttemptDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A attempt digest as the attempt team keeps it. */
data class AttemptDigest(
    var code: String,
    var label: String,
    var checksum: String,
    var tags: MutableList<String>,
    var archived: Boolean,
    var score: Double,
    var status: AttemptDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptDigestStatus): Boolean {
        when (status) {
            AttemptDigestStatus.NEW -> return next == AttemptDigestStatus.FAILED
            AttemptDigestStatus.QUEUED -> return next == AttemptDigestStatus.FAILED
            AttemptDigestStatus.RUNNING -> return next == AttemptDigestStatus.DONE || next == AttemptDigestStatus.FAILED
            AttemptDigestStatus.DONE -> return next == AttemptDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AttemptDigestStatus) {
        if (!canMoveTo(next)) {
            error("attempt digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"attempt digest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = score

    fun snapshot(): AttemptDigest {
        return AttemptDigest(
            code = code,
            label = label,
            checksum = checksum,
            tags = tags,
            archived = archived,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a attempt digest in status new. */
fun newAttemptDigest(code: String, label: String): AttemptDigest {
    return AttemptDigest(
        code = code,
        label = label,
        checksum = "",
        tags = mutableListOf<String>(),
        archived = false,
        score = 0.0,
        status = AttemptDigestStatus.NEW,
    )
}
