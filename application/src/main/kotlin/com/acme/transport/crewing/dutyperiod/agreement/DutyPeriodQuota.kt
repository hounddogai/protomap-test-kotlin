package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period quota is in its life cycle. */
enum class DutyPeriodQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A duty period quota as the duty period team keeps it. */
data class DutyPeriodQuota(
    var code: String,
    var title: String,
    var visible: Boolean,
    var loadKg: Double,
    var archived: Boolean,
    var margin: Double,
    var label: String,
    var carrier: String,
    var discount: Double,
    var dock: String,
    var status: DutyPeriodQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodQuotaStatus): Boolean {
        when (status) {
            DutyPeriodQuotaStatus.CREATED -> return next == DutyPeriodQuotaStatus.VALIDATED || next == DutyPeriodQuotaStatus.PUBLISHED
            DutyPeriodQuotaStatus.VALIDATED -> {
                when (next) {
                    DutyPeriodQuotaStatus.RETIRED -> return true
                }
                return false
            }
            DutyPeriodQuotaStatus.PUBLISHED -> {
                when (next) {
                    DutyPeriodQuotaStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodQuotaStatus) {
        if (!canMoveTo(next)) {
            error("duty period quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun effectiveVolume(): Double = round(margin * loadKg * 100) / 100

    fun snapshot(): DutyPeriodQuota {
        return DutyPeriodQuota(
            code = code,
            title = title,
            visible = visible,
            loadKg = loadKg,
            archived = archived,
            margin = margin,
            label = label,
            carrier = carrier,
            discount = discount,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a duty period quota in status created. */
fun newDutyPeriodQuota(code: String, title: String): DutyPeriodQuota {
    return DutyPeriodQuota(
        code = code,
        title = title,
        visible = false,
        loadKg = 0.0,
        archived = false,
        margin = 0.0,
        label = "",
        carrier = "",
        discount = 0.0,
        dock = "",
        status = DutyPeriodQuotaStatus.CREATED,
    )
}
