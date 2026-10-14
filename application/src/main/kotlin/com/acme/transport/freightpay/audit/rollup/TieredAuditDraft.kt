package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered audit draft is in its life cycle. */
enum class TieredAuditDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered audit draft. Its code is unique within a tenant. */
data class TieredAuditDraft(
    var code: String,
    var label: String,
    var archived: Boolean,
    var revision: Int,
    var ownerId: String,
    var vehicleId: String,
    var fuelLiters: Double,
    var region: String,
    var status: TieredAuditDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAuditDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAuditDraftStatus): Boolean {
        when (status) {
            TieredAuditDraftStatus.NEW -> return next == TieredAuditDraftStatus.FAILED
            TieredAuditDraftStatus.QUEUED -> {
                when (next) {
                    TieredAuditDraftStatus.RUNNING, TieredAuditDraftStatus.FAILED -> return true
                }
                return false
            }
            TieredAuditDraftStatus.RUNNING -> return next == TieredAuditDraftStatus.DONE || next == TieredAuditDraftStatus.FAILED
            TieredAuditDraftStatus.DONE -> return next == TieredAuditDraftStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredAuditDraftStatus) {
        if (!canMoveTo(next)) {
            error("tiered audit draft $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round((revision.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): TieredAuditDraft {
        return TieredAuditDraft(
            code = code,
            label = label,
            archived = archived,
            revision = revision,
            ownerId = ownerId,
            vehicleId = vehicleId,
            fuelLiters = fuelLiters,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered audit draft in status new. */
fun newTieredAuditDraft(code: String, label: String): TieredAuditDraft {
    return TieredAuditDraft(
        code = code,
        label = label,
        archived = false,
        revision = 0,
        ownerId = "",
        vehicleId = "",
        fuelLiters = 0.0,
        region = "",
        status = TieredAuditDraftStatus.NEW,
    )
}
