package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled defect entry is in its life cycle. */
enum class ScheduledDefectEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled defect entry. Its code is unique within a tenant. */
data class ScheduledDefectEntry(
    var code: String,
    var title: String,
    var region: String,
    var label: String,
    var expiresAt: Int,
    var revision: Int,
    var timezone: String,
    var lane: String,
    var dock: String,
    var depot: String,
    var attributes: MutableMap<String, String>,
    var status: ScheduledDefectEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDefectEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDefectEntryStatus): Boolean {
        when (status) {
            ScheduledDefectEntryStatus.PROPOSED -> return next == ScheduledDefectEntryStatus.REJECTED
            ScheduledDefectEntryStatus.APPROVED -> return next == ScheduledDefectEntryStatus.REJECTED
            ScheduledDefectEntryStatus.REJECTED -> {
                when (next) {
                    ScheduledDefectEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDefectEntryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled defect entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round(revision.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledDefectEntry {
        return ScheduledDefectEntry(
            code = code,
            title = title,
            region = region,
            label = label,
            expiresAt = expiresAt,
            revision = revision,
            timezone = timezone,
            lane = lane,
            dock = dock,
            depot = depot,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled defect entry in status proposed. */
fun newScheduledDefectEntry(code: String, title: String): ScheduledDefectEntry {
    return ScheduledDefectEntry(
        code = code,
        title = title,
        region = "",
        label = "",
        expiresAt = 0,
        revision = 0,
        timezone = "",
        lane = "",
        dock = "",
        depot = "",
        attributes = mutableMapOf<String, String>(),
        status = ScheduledDefectEntryStatus.PROPOSED,
    )
}
