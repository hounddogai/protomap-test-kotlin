package com.acme.transport.fleetops.defect.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive defect record is in its life cycle. */
enum class AdaptiveDefectRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One adaptive defect record, with what the planners need to act on it. */
data class AdaptiveDefectRecord(
    var code: String,
    var name: String,
    var dock: String,
    var quantity: Int,
    var etaMinutes: Int,
    var description: String,
    var depot: String,
    var reviewer: String,
    var endsAt: Int,
    var active: Boolean,
    var status: AdaptiveDefectRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDefectRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDefectRecordStatus): Boolean {
        when (status) {
            AdaptiveDefectRecordStatus.PROPOSED -> {
                when (next) {
                    AdaptiveDefectRecordStatus.APPROVED, AdaptiveDefectRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveDefectRecordStatus.APPROVED -> {
                when (next) {
                    AdaptiveDefectRecordStatus.REJECTED, AdaptiveDefectRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveDefectRecordStatus.REJECTED -> return next == AdaptiveDefectRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AdaptiveDefectRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive defect record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((quantity.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDefectRecord {
        return AdaptiveDefectRecord(
            code = code,
            name = name,
            dock = dock,
            quantity = quantity,
            etaMinutes = etaMinutes,
            description = description,
            depot = depot,
            reviewer = reviewer,
            endsAt = endsAt,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive defect record in status proposed. */
fun newAdaptiveDefectRecord(code: String, name: String): AdaptiveDefectRecord {
    return AdaptiveDefectRecord(
        code = code,
        name = name,
        dock = "",
        quantity = 0,
        etaMinutes = 0,
        description = "",
        depot = "",
        reviewer = "",
        endsAt = 0,
        active = false,
        status = AdaptiveDefectRecordStatus.PROPOSED,
    )
}
