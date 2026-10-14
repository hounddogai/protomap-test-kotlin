package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional ev charge manifest records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a regional ev charge manifest is in its life cycle. */
enum class RegionalEvChargeManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional ev charge manifest. Its code is unique within a tenant. */
data class RegionalEvChargeManifest(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var revision: Int,
    var dock: String,
    var batchSize: Int,
    var visible: Boolean,
    var loadKg: Double,
    var attempts: Int,
    var active: Boolean,
    var etaMinutes: Int,
    var status: RegionalEvChargeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalEvChargeManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalEvChargeManifestStatus): Boolean {
        when (status) {
            RegionalEvChargeManifestStatus.NEW -> return next == RegionalEvChargeManifestStatus.DONE || next == RegionalEvChargeManifestStatus.FAILED
            RegionalEvChargeManifestStatus.QUEUED -> return next == RegionalEvChargeManifestStatus.RUNNING || next == RegionalEvChargeManifestStatus.DONE
            RegionalEvChargeManifestStatus.RUNNING -> {
                when (next) {
                    RegionalEvChargeManifestStatus.DONE, RegionalEvChargeManifestStatus.FAILED -> return true
                }
                return false
            }
            RegionalEvChargeManifestStatus.DONE -> return next == RegionalEvChargeManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalEvChargeManifestStatus) {
        if (!canMoveTo(next)) {
            error("regional ev charge manifest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun netExposure(): Double = round((updatedAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): RegionalEvChargeManifest {
        return RegionalEvChargeManifest(
            code = code,
            label = label,
            updatedAt = updatedAt,
            revision = revision,
            dock = dock,
            batchSize = batchSize,
            visible = visible,
            loadKg = loadKg,
            attempts = attempts,
            active = active,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional ev charge manifest in status new. */
fun newRegionalEvChargeManifest(code: String, label: String): RegionalEvChargeManifest {
    return RegionalEvChargeManifest(
        code = code,
        label = label,
        updatedAt = 0,
        revision = 0,
        dock = "",
        batchSize = 0,
        visible = false,
        loadKg = 0.0,
        attempts = 0,
        active = false,
        etaMinutes = 0,
        status = RegionalEvChargeManifestStatus.NEW,
    )
}
