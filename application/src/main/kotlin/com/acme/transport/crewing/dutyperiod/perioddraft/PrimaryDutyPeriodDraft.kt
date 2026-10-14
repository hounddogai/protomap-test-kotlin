package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary duty period draft records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a primary duty period draft is in its life cycle. */
enum class PrimaryDutyPeriodDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary duty period draft. Its code is unique within a tenant. */
data class PrimaryDutyPeriodDraft(
    var code: String,
    var label: String,
    var loadKg: Double,
    var attempts: Int,
    var locale: String,
    var category: String,
    var timezone: String,
    var routeCode: String,
    var title: String,
    var checksum: String,
    var floorValue: Double,
    var status: PrimaryDutyPeriodDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDutyPeriodDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDutyPeriodDraftStatus): Boolean {
        when (status) {
            PrimaryDutyPeriodDraftStatus.PROPOSED -> return next == PrimaryDutyPeriodDraftStatus.APPROVED || next == PrimaryDutyPeriodDraftStatus.REJECTED
            PrimaryDutyPeriodDraftStatus.APPROVED -> {
                when (next) {
                    PrimaryDutyPeriodDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryDutyPeriodDraftStatus.REJECTED -> return next == PrimaryDutyPeriodDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PrimaryDutyPeriodDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary duty period draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round((floorValue + attempts.toDouble()) * 100) / 100

    fun snapshot(): PrimaryDutyPeriodDraft {
        return PrimaryDutyPeriodDraft(
            code = code,
            label = label,
            loadKg = loadKg,
            attempts = attempts,
            locale = locale,
            category = category,
            timezone = timezone,
            routeCode = routeCode,
            title = title,
            checksum = checksum,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary duty period draft in status proposed. */
fun newPrimaryDutyPeriodDraft(code: String, label: String): PrimaryDutyPeriodDraft {
    return PrimaryDutyPeriodDraft(
        code = code,
        label = label,
        loadKg = 0.0,
        attempts = 0,
        locale = "",
        category = "",
        timezone = "",
        routeCode = "",
        title = "",
        checksum = "",
        floorValue = 0.0,
        status = PrimaryDutyPeriodDraftStatus.PROPOSED,
    )
}
