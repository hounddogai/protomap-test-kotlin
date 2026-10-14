package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional parcel draft is in its life cycle. */
enum class RegionalParcelDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional parcel draft. Its code is unique within a tenant. */
data class RegionalParcelDraft(
    var code: String,
    var label: String,
    var externalRef: String,
    var ownerId: String,
    var driverId: String,
    var unit: String,
    var retries: Int,
    var ratio: Double,
    var status: RegionalParcelDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelDraftStatus): Boolean {
        when (status) {
            RegionalParcelDraftStatus.IDLE -> return next == RegionalParcelDraftStatus.WARMING
            RegionalParcelDraftStatus.WARMING -> {
                when (next) {
                    RegionalParcelDraftStatus.STOPPED -> return true
                }
                return false
            }
            RegionalParcelDraftStatus.READY -> return next == RegionalParcelDraftStatus.STOPPED
            RegionalParcelDraftStatus.DRAINING -> {
                when (next) {
                    RegionalParcelDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalParcelDraftStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedTotal(): Double = round((retries.toDouble() - ratio) * 100) / 100

    fun snapshot(): RegionalParcelDraft {
        return RegionalParcelDraft(
            code = code,
            label = label,
            externalRef = externalRef,
            ownerId = ownerId,
            driverId = driverId,
            unit = unit,
            retries = retries,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional parcel draft in status idle. */
fun newRegionalParcelDraft(code: String, label: String): RegionalParcelDraft {
    return RegionalParcelDraft(
        code = code,
        label = label,
        externalRef = "",
        ownerId = "",
        driverId = "",
        unit = "",
        retries = 0,
        ratio = 0.0,
        status = RegionalParcelDraftStatus.IDLE,
    )
}
