package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection digest records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection digest is in its life cycle. */
enum class InspectionDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One inspection digest, with what the planners need to act on it. */
data class InspectionDigest(
    var code: String,
    var label: String,
    var unit: String,
    var externalRef: String,
    var etaMinutes: Int,
    var score: Double,
    var ownerId: String,
    var quantity: Int,
    var status: InspectionDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionDigestStatus): Boolean {
        when (status) {
            InspectionDigestStatus.NEW -> {
                when (next) {
                    InspectionDigestStatus.DONE -> return true
                }
                return false
            }
            InspectionDigestStatus.QUEUED -> {
                when (next) {
                    InspectionDigestStatus.RUNNING -> return true
                }
                return false
            }
            InspectionDigestStatus.RUNNING -> {
                when (next) {
                    InspectionDigestStatus.FAILED -> return true
                }
                return false
            }
            InspectionDigestStatus.DONE -> {
                when (next) {
                    InspectionDigestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionDigestStatus) {
        if (!canMoveTo(next)) {
            error("inspection digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((score + quantity.toDouble()) * 100) / 100

    fun snapshot(): InspectionDigest {
        return InspectionDigest(
            code = code,
            label = label,
            unit = unit,
            externalRef = externalRef,
            etaMinutes = etaMinutes,
            score = score,
            ownerId = ownerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a inspection digest in status new. */
fun newInspectionDigest(code: String, label: String): InspectionDigest {
    return InspectionDigest(
        code = code,
        label = label,
        unit = "",
        externalRef = "",
        etaMinutes = 0,
        score = 0.0,
        ownerId = "",
        quantity = 0,
        status = InspectionDigestStatus.NEW,
    )
}
