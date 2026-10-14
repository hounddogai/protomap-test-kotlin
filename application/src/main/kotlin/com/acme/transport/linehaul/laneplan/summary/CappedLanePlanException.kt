package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped lane plan exception is in its life cycle. */
enum class CappedLanePlanExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped lane plan exception as the lane plan team keeps it. */
data class CappedLanePlanException(
    var code: String,
    var name: String,
    var region: String,
    var ceiling: Double,
    var vehicleId: String,
    var routeCode: String,
    var source: String,
    var checksum: String,
    var discount: Double,
    var label: String,
    var retries: Int,
    var status: CappedLanePlanExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLanePlanExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLanePlanExceptionStatus): Boolean {
        when (status) {
            CappedLanePlanExceptionStatus.OPEN -> {
                when (next) {
                    CappedLanePlanExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedLanePlanExceptionStatus.IN_REVIEW -> {
                when (next) {
                    CappedLanePlanExceptionStatus.REOPENED -> return true
                }
                return false
            }
            CappedLanePlanExceptionStatus.RESOLVED -> return next == CappedLanePlanExceptionStatus.REOPENED
            CappedLanePlanExceptionStatus.REOPENED -> {
                when (next) {
                    CappedLanePlanExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedLanePlanExceptionStatus) {
        if (!canMoveTo(next)) {
            error("capped lane plan exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun projectedValue(): Double = round((ceiling + discount) * 100) / 100

    fun snapshot(): CappedLanePlanException {
        return CappedLanePlanException(
            code = code,
            name = name,
            region = region,
            ceiling = ceiling,
            vehicleId = vehicleId,
            routeCode = routeCode,
            source = source,
            checksum = checksum,
            discount = discount,
            label = label,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped lane plan exception in status open. */
fun newCappedLanePlanException(code: String, name: String): CappedLanePlanException {
    return CappedLanePlanException(
        code = code,
        name = name,
        region = "",
        ceiling = 0.0,
        vehicleId = "",
        routeCode = "",
        source = "",
        checksum = "",
        discount = 0.0,
        label = "",
        retries = 0,
        status = CappedLanePlanExceptionStatus.OPEN,
    )
}
