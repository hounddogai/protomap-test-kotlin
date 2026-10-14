package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global duty period record is in its life cycle. */
enum class GlobalDutyPeriodRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global duty period record, with what the planners need to act on it. */
data class GlobalDutyPeriodRecord(
    var code: String,
    var title: String,
    var capacity: Int,
    var pinned: Boolean,
    var shiftCode: String,
    var description: String,
    var depot: String,
    var ratio: Double,
    var label: String,
    var floorValue: Double,
    var status: GlobalDutyPeriodRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDutyPeriodRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDutyPeriodRecordStatus): Boolean {
        when (status) {
            GlobalDutyPeriodRecordStatus.CREATED -> {
                when (next) {
                    GlobalDutyPeriodRecordStatus.VALIDATED, GlobalDutyPeriodRecordStatus.RETIRED -> return true
                }
                return false
            }
            GlobalDutyPeriodRecordStatus.VALIDATED -> {
                when (next) {
                    GlobalDutyPeriodRecordStatus.RETIRED -> return true
                }
                return false
            }
            GlobalDutyPeriodRecordStatus.PUBLISHED -> return next == GlobalDutyPeriodRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalDutyPeriodRecordStatus) {
        if (!canMoveTo(next)) {
            error("global duty period record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global duty period record"} $code"

    fun netValue(): Double = round(capacity.toDouble() * floorValue * 100) / 100

    fun snapshot(): GlobalDutyPeriodRecord {
        return GlobalDutyPeriodRecord(
            code = code,
            title = title,
            capacity = capacity,
            pinned = pinned,
            shiftCode = shiftCode,
            description = description,
            depot = depot,
            ratio = ratio,
            label = label,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global duty period record in status created. */
fun newGlobalDutyPeriodRecord(code: String, title: String): GlobalDutyPeriodRecord {
    return GlobalDutyPeriodRecord(
        code = code,
        title = title,
        capacity = 0,
        pinned = false,
        shiftCode = "",
        description = "",
        depot = "",
        ratio = 0.0,
        label = "",
        floorValue = 0.0,
        status = GlobalDutyPeriodRecordStatus.CREATED,
    )
}
