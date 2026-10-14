package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal inspection digest records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a seasonal inspection digest is in its life cycle. */
enum class SeasonalInspectionDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal inspection digest. Its code is unique within a tenant. */
data class SeasonalInspectionDigest(
    var code: String,
    var label: String,
    var ceiling: Double,
    var capacity: Int,
    var notes: String,
    var revision: Int,
    var ratio: Double,
    var amount: Double,
    var status: SeasonalInspectionDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalInspectionDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalInspectionDigestStatus): Boolean {
        when (status) {
            SeasonalInspectionDigestStatus.REQUESTED -> {
                when (next) {
                    SeasonalInspectionDigestStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalInspectionDigestStatus.GRANTED -> return next == SeasonalInspectionDigestStatus.REVOKED || next == SeasonalInspectionDigestStatus.EXPIRED
            SeasonalInspectionDigestStatus.REVOKED -> {
                when (next) {
                    SeasonalInspectionDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalInspectionDigestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal inspection digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((amount + revision.toDouble()) * 100) / 100

    fun snapshot(): SeasonalInspectionDigest {
        return SeasonalInspectionDigest(
            code = code,
            label = label,
            ceiling = ceiling,
            capacity = capacity,
            notes = notes,
            revision = revision,
            ratio = ratio,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal inspection digest in status requested. */
fun newSeasonalInspectionDigest(code: String, label: String): SeasonalInspectionDigest {
    return SeasonalInspectionDigest(
        code = code,
        label = label,
        ceiling = 0.0,
        capacity = 0,
        notes = "",
        revision = 0,
        ratio = 0.0,
        amount = 0.0,
        status = SeasonalInspectionDigestStatus.REQUESTED,
    )
}
