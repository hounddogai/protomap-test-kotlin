package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal inspection request is in its life cycle. */
enum class SeasonalInspectionRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One seasonal inspection request, with what the planners need to act on it. */
data class SeasonalInspectionRequest(
    var code: String,
    var name: String,
    var checksum: String,
    var shiftCode: String,
    var region: String,
    var driverId: String,
    var priority: Int,
    var description: String,
    var maximum: Double,
    var limit: Int,
    var etaMinutes: Int,
    var status: SeasonalInspectionRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalInspectionRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalInspectionRequestStatus): Boolean {
        when (status) {
            SeasonalInspectionRequestStatus.PROPOSED -> return next == SeasonalInspectionRequestStatus.APPROVED || next == SeasonalInspectionRequestStatus.WITHDRAWN
            SeasonalInspectionRequestStatus.APPROVED -> return next == SeasonalInspectionRequestStatus.REJECTED || next == SeasonalInspectionRequestStatus.WITHDRAWN
            SeasonalInspectionRequestStatus.REJECTED -> {
                when (next) {
                    SeasonalInspectionRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalInspectionRequestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal inspection request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedExposure(): Double = round((etaMinutes.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): SeasonalInspectionRequest {
        return SeasonalInspectionRequest(
            code = code,
            name = name,
            checksum = checksum,
            shiftCode = shiftCode,
            region = region,
            driverId = driverId,
            priority = priority,
            description = description,
            maximum = maximum,
            limit = limit,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal inspection request in status proposed. */
fun newSeasonalInspectionRequest(code: String, name: String): SeasonalInspectionRequest {
    return SeasonalInspectionRequest(
        code = code,
        name = name,
        checksum = "",
        shiftCode = "",
        region = "",
        driverId = "",
        priority = 0,
        description = "",
        maximum = 0.0,
        limit = 0,
        etaMinutes = 0,
        status = SeasonalInspectionRequestStatus.PROPOSED,
    )
}
