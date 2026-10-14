package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending qualification audit is in its life cycle. */
enum class PendingQualificationAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending qualification audit as the qualification team keeps it. */
data class PendingQualificationAudit(
    var code: String,
    var name: String,
    var limit: Int,
    var active: Boolean,
    var attributes: MutableMap<String, String>,
    var dock: String,
    var reviewer: String,
    var startsAt: Int,
    var approved: Boolean,
    var graceDays: Int,
    var distanceKm: Double,
    var status: PendingQualificationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingQualificationAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingQualificationAuditStatus): Boolean {
        when (status) {
            PendingQualificationAuditStatus.NEW -> return next == PendingQualificationAuditStatus.DONE || next == PendingQualificationAuditStatus.FAILED
            PendingQualificationAuditStatus.QUEUED -> return next == PendingQualificationAuditStatus.RUNNING || next == PendingQualificationAuditStatus.DONE
            PendingQualificationAuditStatus.RUNNING -> return next == PendingQualificationAuditStatus.DONE || next == PendingQualificationAuditStatus.FAILED
            PendingQualificationAuditStatus.DONE -> {
                when (next) {
                    PendingQualificationAuditStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingQualificationAuditStatus) {
        if (!canMoveTo(next)) {
            error("pending qualification audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending qualification audit"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((startsAt.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): PendingQualificationAudit {
        return PendingQualificationAudit(
            code = code,
            name = name,
            limit = limit,
            active = active,
            attributes = attributes,
            dock = dock,
            reviewer = reviewer,
            startsAt = startsAt,
            approved = approved,
            graceDays = graceDays,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending qualification audit in status new. */
fun newPendingQualificationAudit(code: String, name: String): PendingQualificationAudit {
    return PendingQualificationAudit(
        code = code,
        name = name,
        limit = 0,
        active = false,
        attributes = mutableMapOf<String, String>(),
        dock = "",
        reviewer = "",
        startsAt = 0,
        approved = false,
        graceDays = 0,
        distanceKm = 0.0,
        status = PendingQualificationAuditStatus.NEW,
    )
}
