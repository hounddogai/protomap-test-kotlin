package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.text.normalize

/** Where a inspection record is in its life cycle. */
enum class InspectionRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a inspection record and its current status. */
data class InspectionRecord(
    var code: String,
    var title: String,
    var archived: Boolean,
    var timezone: String,
    var priority: Int,
    var status: InspectionRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionRecordStatus): Boolean {
        when (status) {
            InspectionRecordStatus.CREATED -> {
                when (next) {
                    InspectionRecordStatus.PUBLISHED -> return true
                }
                return false
            }
            InspectionRecordStatus.VALIDATED -> return next == InspectionRecordStatus.PUBLISHED
            InspectionRecordStatus.PUBLISHED -> {
                when (next) {
                    InspectionRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionRecordStatus) {
        if (!canMoveTo(next)) {
            error("inspection record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection record"} $code"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): InspectionRecord {
        return InspectionRecord(
            code = code,
            title = title,
            archived = archived,
            timezone = timezone,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection record in status created. */
fun newInspectionRecord(code: String, title: String): InspectionRecord {
    return InspectionRecord(
        code = code,
        title = title,
        archived = false,
        timezone = "",
        priority = 0,
        status = InspectionRecordStatus.CREATED,
    )
}
