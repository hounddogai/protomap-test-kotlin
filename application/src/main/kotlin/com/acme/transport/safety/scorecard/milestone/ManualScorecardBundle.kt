package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual scorecard bundle is in its life cycle. */
enum class ManualScorecardBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a manual scorecard bundle and its current status. */
data class ManualScorecardBundle(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var contactEmail: String,
    var loadKg: Double,
    var pallets: Int,
    var summary: String,
    var status: ManualScorecardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualScorecardBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualScorecardBundleStatus): Boolean {
        when (status) {
            ManualScorecardBundleStatus.NEW -> {
                when (next) {
                    ManualScorecardBundleStatus.RUNNING, ManualScorecardBundleStatus.FAILED -> return true
                }
                return false
            }
            ManualScorecardBundleStatus.QUEUED -> return next == ManualScorecardBundleStatus.DONE || next == ManualScorecardBundleStatus.FAILED
            ManualScorecardBundleStatus.RUNNING -> {
                when (next) {
                    ManualScorecardBundleStatus.DONE, ManualScorecardBundleStatus.FAILED -> return true
                }
                return false
            }
            ManualScorecardBundleStatus.DONE -> return next == ManualScorecardBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualScorecardBundleStatus) {
        if (!canMoveTo(next)) {
            error("manual scorecard bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual scorecard bundle"} $code"

    fun adjustedVolume(): Double = round((loadKg + pallets.toDouble()) * 100) / 100

    fun snapshot(): ManualScorecardBundle {
        return ManualScorecardBundle(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            contactEmail = contactEmail,
            loadKg = loadKg,
            pallets = pallets,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual scorecard bundle in status new. */
fun newManualScorecardBundle(code: String, label: String): ManualScorecardBundle {
    return ManualScorecardBundle(
        code = code,
        label = label,
        etaMinutes = 0,
        contactEmail = "",
        loadKg = 0.0,
        pallets = 0,
        summary = "",
        status = ManualScorecardBundleStatus.NEW,
    )
}
