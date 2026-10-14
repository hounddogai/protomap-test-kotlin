package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.text.normalize

/** Where a inspection notice is in its life cycle. */
enum class InspectionNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A inspection notice as the inspection team keeps it. */
data class InspectionNotice(
    var code: String,
    var name: String,
    var source: String,
    var country: String,
    var unit: String,
    var score: Double,
    var status: InspectionNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionNoticeStatus): Boolean {
        when (status) {
            InspectionNoticeStatus.REQUESTED -> {
                when (next) {
                    InspectionNoticeStatus.GRANTED, InspectionNoticeStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionNoticeStatus.GRANTED -> return next == InspectionNoticeStatus.EXPIRED
            InspectionNoticeStatus.REVOKED -> {
                when (next) {
                    InspectionNoticeStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionNoticeStatus) {
        if (!canMoveTo(next)) {
            error("inspection notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = score

    fun snapshot(): InspectionNotice {
        return InspectionNotice(
            code = code,
            name = name,
            source = source,
            country = country,
            unit = unit,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a inspection notice in status requested. */
fun newInspectionNotice(code: String, name: String): InspectionNotice {
    return InspectionNotice(
        code = code,
        name = name,
        source = "",
        country = "",
        unit = "",
        score = 0.0,
        status = InspectionNoticeStatus.REQUESTED,
    )
}
