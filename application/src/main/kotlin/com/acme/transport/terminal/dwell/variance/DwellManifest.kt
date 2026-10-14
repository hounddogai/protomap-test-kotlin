package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell manifest is in its life cycle. */
enum class DwellManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a dwell manifest and its current status. */
data class DwellManifest(
    var code: String,
    var title: String,
    var sequence: Int,
    var country: String,
    var budget: Double,
    var active: Boolean,
    var name: String,
    var checksum: String,
    var status: DwellManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellManifestStatus): Boolean {
        when (status) {
            DwellManifestStatus.REQUESTED -> {
                when (next) {
                    DwellManifestStatus.GRANTED -> return true
                }
                return false
            }
            DwellManifestStatus.GRANTED -> return next == DwellManifestStatus.REVOKED || next == DwellManifestStatus.EXPIRED
            DwellManifestStatus.REVOKED -> {
                when (next) {
                    DwellManifestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellManifestStatus) {
        if (!canMoveTo(next)) {
            error("dwell manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((sequence.toDouble() - budget) * 100) / 100

    fun snapshot(): DwellManifest {
        return DwellManifest(
            code = code,
            title = title,
            sequence = sequence,
            country = country,
            budget = budget,
            active = active,
            name = name,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dwell manifest in status requested. */
fun newDwellManifest(code: String, title: String): DwellManifest {
    return DwellManifest(
        code = code,
        title = title,
        sequence = 0,
        country = "",
        budget = 0.0,
        active = false,
        name = "",
        checksum = "",
        status = DwellManifestStatus.REQUESTED,
    )
}
