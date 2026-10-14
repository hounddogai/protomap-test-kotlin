package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dock door tier is in its life cycle. */
enum class RegionalDockDoorTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional dock door tier. Its code is unique within a tenant. */
data class RegionalDockDoorTier(
    var code: String,
    var label: String,
    var margin: Double,
    var summary: String,
    var threshold: Double,
    var status: RegionalDockDoorTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDockDoorTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDockDoorTierStatus): Boolean {
        when (status) {
            RegionalDockDoorTierStatus.PROPOSED -> {
                when (next) {
                    RegionalDockDoorTierStatus.REJECTED, RegionalDockDoorTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalDockDoorTierStatus.APPROVED -> {
                when (next) {
                    RegionalDockDoorTierStatus.REJECTED, RegionalDockDoorTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalDockDoorTierStatus.REJECTED -> {
                when (next) {
                    RegionalDockDoorTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDockDoorTierStatus) {
        if (!canMoveTo(next)) {
            error("regional dock door tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional dock door tier"} $code"

    fun netVolume(): Double = round((margin - threshold) * 100) / 100

    fun snapshot(): RegionalDockDoorTier {
        return RegionalDockDoorTier(
            code = code,
            label = label,
            margin = margin,
            summary = summary,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional dock door tier in status proposed. */
fun newRegionalDockDoorTier(code: String, label: String): RegionalDockDoorTier {
    return RegionalDockDoorTier(
        code = code,
        label = label,
        margin = 0.0,
        summary = "",
        threshold = 0.0,
        status = RegionalDockDoorTierStatus.PROPOSED,
    )
}
