package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report preference is in its life cycle. */
enum class CarbonReportPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A carbon report preference as the carbon report team keeps it. */
data class CarbonReportPreference(
    var code: String,
    var label: String,
    var version: Int,
    var reviewer: String,
    var priority: Int,
    var checksum: String,
    var archived: Boolean,
    var batchSize: Int,
    var carrier: String,
    var status: CarbonReportPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportPreferenceStatus): Boolean {
        when (status) {
            CarbonReportPreferenceStatus.NEW -> return next == CarbonReportPreferenceStatus.RUNNING || next == CarbonReportPreferenceStatus.DONE
            CarbonReportPreferenceStatus.QUEUED -> return next == CarbonReportPreferenceStatus.FAILED
            CarbonReportPreferenceStatus.RUNNING -> {
                when (next) {
                    CarbonReportPreferenceStatus.FAILED -> return true
                }
                return false
            }
            CarbonReportPreferenceStatus.DONE -> {
                when (next) {
                    CarbonReportPreferenceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("carbon report preference $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((batchSize.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportPreference {
        return CarbonReportPreference(
            code = code,
            label = label,
            version = version,
            reviewer = reviewer,
            priority = priority,
            checksum = checksum,
            archived = archived,
            batchSize = batchSize,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a carbon report preference in status new. */
fun newCarbonReportPreference(code: String, label: String): CarbonReportPreference {
    return CarbonReportPreference(
        code = code,
        label = label,
        version = 0,
        reviewer = "",
        priority = 0,
        checksum = "",
        archived = false,
        batchSize = 0,
        carrier = "",
        status = CarbonReportPreferenceStatus.NEW,
    )
}
