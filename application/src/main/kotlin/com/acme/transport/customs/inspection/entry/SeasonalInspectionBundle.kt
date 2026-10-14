package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal inspection bundle is in its life cycle. */
enum class SeasonalInspectionBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal inspection bundle. Its code is unique within a tenant. */
data class SeasonalInspectionBundle(
    var code: String,
    var name: String,
    var limit: Int,
    var startsAt: Int,
    var sequence: Int,
    var active: Boolean,
    var checksum: String,
    var discount: Double,
    var status: SeasonalInspectionBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalInspectionBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalInspectionBundleStatus): Boolean {
        when (status) {
            SeasonalInspectionBundleStatus.PLANNED -> return next == SeasonalInspectionBundleStatus.SCHEDULED
            SeasonalInspectionBundleStatus.SCHEDULED -> {
                when (next) {
                    SeasonalInspectionBundleStatus.IN_PROGRESS -> return true
                }
                return false
            }
            SeasonalInspectionBundleStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalInspectionBundleStatus.COMPLETED, SeasonalInspectionBundleStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalInspectionBundleStatus.COMPLETED -> {
                when (next) {
                    SeasonalInspectionBundleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalInspectionBundleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal inspection bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal inspection bundle"} $code"

    fun weightedVolume(): Double = round((sequence.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalInspectionBundle {
        return SeasonalInspectionBundle(
            code = code,
            name = name,
            limit = limit,
            startsAt = startsAt,
            sequence = sequence,
            active = active,
            checksum = checksum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal inspection bundle in status planned. */
fun newSeasonalInspectionBundle(code: String, name: String): SeasonalInspectionBundle {
    return SeasonalInspectionBundle(
        code = code,
        name = name,
        limit = 0,
        startsAt = 0,
        sequence = 0,
        active = false,
        checksum = "",
        discount = 0.0,
        status = SeasonalInspectionBundleStatus.PLANNED,
    )
}
