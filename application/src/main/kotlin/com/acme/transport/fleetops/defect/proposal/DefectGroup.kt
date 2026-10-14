package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect group is in its life cycle. */
enum class DefectGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A defect group. Its code is unique within a tenant. */
data class DefectGroup(
    var code: String,
    var title: String,
    var ratio: Double,
    var depot: String,
    var quantity: Int,
    var shiftCode: String,
    var status: DefectGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectGroupStatus): Boolean {
        when (status) {
            DefectGroupStatus.PROPOSED -> {
                when (next) {
                    DefectGroupStatus.REJECTED -> return true
                }
                return false
            }
            DefectGroupStatus.APPROVED -> return next == DefectGroupStatus.REJECTED || next == DefectGroupStatus.WITHDRAWN
            DefectGroupStatus.REJECTED -> return next == DefectGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefectGroupStatus) {
        if (!canMoveTo(next)) {
            error("defect group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round((quantity.toDouble() + ratio) * 100) / 100

    fun snapshot(): DefectGroup {
        return DefectGroup(
            code = code,
            title = title,
            ratio = ratio,
            depot = depot,
            quantity = quantity,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a defect group in status proposed. */
fun newDefectGroup(code: String, title: String): DefectGroup {
    return DefectGroup(
        code = code,
        title = title,
        ratio = 0.0,
        depot = "",
        quantity = 0,
        shiftCode = "",
        status = DefectGroupStatus.PROPOSED,
    )
}
