package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback inspection notice is in its life cycle. */
enum class FallbackInspectionNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a fallback inspection notice and its current status. */
data class FallbackInspectionNotice(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var approved: Boolean,
    var category: String,
    var description: String,
    var cost: Double,
    var loadKg: Double,
    var notes: String,
    var batchSize: Int,
    var status: FallbackInspectionNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionNoticeStatus): Boolean {
        when (status) {
            FallbackInspectionNoticeStatus.PROPOSED -> {
                when (next) {
                    FallbackInspectionNoticeStatus.REJECTED, FallbackInspectionNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackInspectionNoticeStatus.APPROVED -> {
                when (next) {
                    FallbackInspectionNoticeStatus.REJECTED, FallbackInspectionNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackInspectionNoticeStatus.REJECTED -> return next == FallbackInspectionNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackInspectionNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback inspection notice"} $code"

    fun effectiveTotal(): Double = round((batchSize.toDouble() + cost) * 100) / 100

    fun snapshot(): FallbackInspectionNotice {
        return FallbackInspectionNotice(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            approved = approved,
            category = category,
            description = description,
            cost = cost,
            loadKg = loadKg,
            notes = notes,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback inspection notice in status proposed. */
fun newFallbackInspectionNotice(code: String, name: String): FallbackInspectionNotice {
    return FallbackInspectionNotice(
        code = code,
        name = name,
        etaMinutes = 0,
        approved = false,
        category = "",
        description = "",
        cost = 0.0,
        loadKg = 0.0,
        notes = "",
        batchSize = 0,
        status = FallbackInspectionNoticeStatus.PROPOSED,
    )
}
