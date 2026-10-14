package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.normalize

/** Where a global attempt record is in its life cycle. */
enum class GlobalAttemptRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global attempt record as the attempt team keeps it. */
data class GlobalAttemptRecord(
    var code: String,
    var label: String,
    var reviewer: String,
    var revision: Int,
    var source: String,
    var archived: Boolean,
    var tags: MutableList<String>,
    var status: GlobalAttemptRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAttemptRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAttemptRecordStatus): Boolean {
        when (status) {
            GlobalAttemptRecordStatus.IDLE -> return next == GlobalAttemptRecordStatus.DRAINING
            GlobalAttemptRecordStatus.WARMING -> {
                when (next) {
                    GlobalAttemptRecordStatus.READY, GlobalAttemptRecordStatus.DRAINING -> return true
                }
                return false
            }
            GlobalAttemptRecordStatus.READY -> return next == GlobalAttemptRecordStatus.STOPPED
            GlobalAttemptRecordStatus.DRAINING -> {
                when (next) {
                    GlobalAttemptRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAttemptRecordStatus) {
        if (!canMoveTo(next)) {
            error("global attempt record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): GlobalAttemptRecord {
        return GlobalAttemptRecord(
            code = code,
            label = label,
            reviewer = reviewer,
            revision = revision,
            source = source,
            archived = archived,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global attempt record in status idle. */
fun newGlobalAttemptRecord(code: String, label: String): GlobalAttemptRecord {
    return GlobalAttemptRecord(
        code = code,
        label = label,
        reviewer = "",
        revision = 0,
        source = "",
        archived = false,
        tags = mutableListOf<String>(),
        status = GlobalAttemptRecordStatus.IDLE,
    )
}
