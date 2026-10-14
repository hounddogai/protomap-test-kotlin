package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect bundle records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect bundle is in its life cycle. */
enum class DefectBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a defect bundle and its current status. */
data class DefectBundle(
    var code: String,
    var name: String,
    var threshold: Double,
    var ceiling: Double,
    var durationMinutes: Int,
    var status: DefectBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectBundleStatus): Boolean {
        when (status) {
            DefectBundleStatus.DRAFT -> return next == DefectBundleStatus.PENDING || next == DefectBundleStatus.CLOSED
            DefectBundleStatus.PENDING -> {
                when (next) {
                    DefectBundleStatus.SUSPENDED, DefectBundleStatus.CLOSED -> return true
                }
                return false
            }
            DefectBundleStatus.ACTIVE -> return next == DefectBundleStatus.SUSPENDED || next == DefectBundleStatus.CLOSED
            DefectBundleStatus.SUSPENDED -> {
                when (next) {
                    DefectBundleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectBundleStatus) {
        if (!canMoveTo(next)) {
            error("defect bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((threshold + ceiling) * 100) / 100

    fun snapshot(): DefectBundle {
        return DefectBundle(
            code = code,
            name = name,
            threshold = threshold,
            ceiling = ceiling,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a defect bundle in status draft. */
fun newDefectBundle(code: String, name: String): DefectBundle {
    return DefectBundle(
        code = code,
        name = name,
        threshold = 0.0,
        ceiling = 0.0,
        durationMinutes = 0,
        status = DefectBundleStatus.DRAFT,
    )
}
