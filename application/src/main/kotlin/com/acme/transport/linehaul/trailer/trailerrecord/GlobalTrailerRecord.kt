package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global trailer record records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a global trailer record is in its life cycle. */
enum class GlobalTrailerRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global trailer record. Its code is unique within a tenant. */
data class GlobalTrailerRecord(
    var code: String,
    var name: String,
    var priority: Int,
    var summary: String,
    var archived: Boolean,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var description: String,
    var createdAt: Int,
    var currency: String,
    var status: GlobalTrailerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerRecordStatus): Boolean {
        when (status) {
            GlobalTrailerRecordStatus.OPEN -> {
                when (next) {
                    GlobalTrailerRecordStatus.RESOLVED -> return true
                }
                return false
            }
            GlobalTrailerRecordStatus.IN_REVIEW -> {
                when (next) {
                    GlobalTrailerRecordStatus.RESOLVED -> return true
                }
                return false
            }
            GlobalTrailerRecordStatus.RESOLVED -> return next == GlobalTrailerRecordStatus.REOPENED || next == GlobalTrailerRecordStatus.ARCHIVED
            GlobalTrailerRecordStatus.REOPENED -> return next == GlobalTrailerRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalTrailerRecordStatus) {
        if (!canMoveTo(next)) {
            error("global trailer record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((createdAt.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): GlobalTrailerRecord {
        return GlobalTrailerRecord(
            code = code,
            name = name,
            priority = priority,
            summary = summary,
            archived = archived,
            timezone = timezone,
            attributes = attributes,
            description = description,
            createdAt = createdAt,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global trailer record in status open. */
fun newGlobalTrailerRecord(code: String, name: String): GlobalTrailerRecord {
    return GlobalTrailerRecord(
        code = code,
        name = name,
        priority = 0,
        summary = "",
        archived = false,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        description = "",
        createdAt = 0,
        currency = "",
        status = GlobalTrailerRecordStatus.OPEN,
    )
}
