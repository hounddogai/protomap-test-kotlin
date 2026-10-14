package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped inspection request is in its life cycle. */
enum class CappedInspectionRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a capped inspection request and its current status. */
data class CappedInspectionRequest(
    var code: String,
    var label: String,
    var locale: String,
    var channel: String,
    var ratio: Double,
    var pallets: Int,
    var graceDays: Int,
    var status: CappedInspectionRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionRequestStatus): Boolean {
        when (status) {
            CappedInspectionRequestStatus.DRAFT -> {
                when (next) {
                    CappedInspectionRequestStatus.ACTIVE -> return true
                }
                return false
            }
            CappedInspectionRequestStatus.PENDING -> {
                when (next) {
                    CappedInspectionRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedInspectionRequestStatus.ACTIVE -> {
                when (next) {
                    CappedInspectionRequestStatus.SUSPENDED, CappedInspectionRequestStatus.CLOSED -> return true
                }
                return false
            }
            CappedInspectionRequestStatus.SUSPENDED -> return next == CappedInspectionRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedInspectionRequestStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round(ratio * graceDays.toDouble() * 100) / 100

    fun snapshot(): CappedInspectionRequest {
        return CappedInspectionRequest(
            code = code,
            label = label,
            locale = locale,
            channel = channel,
            ratio = ratio,
            pallets = pallets,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped inspection request in status draft. */
fun newCappedInspectionRequest(code: String, label: String): CappedInspectionRequest {
    return CappedInspectionRequest(
        code = code,
        label = label,
        locale = "",
        channel = "",
        ratio = 0.0,
        pallets = 0,
        graceDays = 0,
        status = CappedInspectionRequestStatus.DRAFT,
    )
}
