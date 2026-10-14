package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect quota is in its life cycle. */
enum class DefectQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One defect quota, with what the planners need to act on it. */
data class DefectQuota(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var budget: Double,
    var shiftCode: String,
    var minimum: Double,
    var contactEmail: String,
    var capacity: Int,
    var amount: Double,
    var label: String,
    var source: String,
    var status: DefectQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectQuotaStatus): Boolean {
        when (status) {
            DefectQuotaStatus.IDLE -> return next == DefectQuotaStatus.DRAINING
            DefectQuotaStatus.WARMING -> {
                when (next) {
                    DefectQuotaStatus.DRAINING -> return true
                }
                return false
            }
            DefectQuotaStatus.READY -> return next == DefectQuotaStatus.STOPPED
            DefectQuotaStatus.DRAINING -> {
                when (next) {
                    DefectQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectQuotaStatus) {
        if (!canMoveTo(next)) {
            error("defect quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((amount + capacity.toDouble()) * 100) / 100

    fun snapshot(): DefectQuota {
        return DefectQuota(
            code = code,
            title = title,
            tags = tags,
            budget = budget,
            shiftCode = shiftCode,
            minimum = minimum,
            contactEmail = contactEmail,
            capacity = capacity,
            amount = amount,
            label = label,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a defect quota in status idle. */
fun newDefectQuota(code: String, title: String): DefectQuota {
    return DefectQuota(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        budget = 0.0,
        shiftCode = "",
        minimum = 0.0,
        contactEmail = "",
        capacity = 0,
        amount = 0.0,
        label = "",
        source = "",
        status = DefectQuotaStatus.IDLE,
    )
}
