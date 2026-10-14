package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional cutoff claim is in its life cycle. */
enum class RegionalCutoffClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional cutoff claim. Its code is unique within a tenant. */
data class RegionalCutoffClaim(
    var code: String,
    var title: String,
    var ceiling: Double,
    var externalRef: String,
    var version: Int,
    var status: RegionalCutoffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCutoffClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCutoffClaimStatus): Boolean {
        when (status) {
            RegionalCutoffClaimStatus.CREATED -> {
                when (next) {
                    RegionalCutoffClaimStatus.VALIDATED, RegionalCutoffClaimStatus.RETIRED -> return true
                }
                return false
            }
            RegionalCutoffClaimStatus.VALIDATED -> {
                when (next) {
                    RegionalCutoffClaimStatus.PUBLISHED, RegionalCutoffClaimStatus.RETIRED -> return true
                }
                return false
            }
            RegionalCutoffClaimStatus.PUBLISHED -> return next == RegionalCutoffClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalCutoffClaimStatus) {
        if (!canMoveTo(next)) {
            error("regional cutoff claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round(ceiling * version.toDouble() * 100) / 100

    fun snapshot(): RegionalCutoffClaim {
        return RegionalCutoffClaim(
            code = code,
            title = title,
            ceiling = ceiling,
            externalRef = externalRef,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional cutoff claim in status created. */
fun newRegionalCutoffClaim(code: String, title: String): RegionalCutoffClaim {
    return RegionalCutoffClaim(
        code = code,
        title = title,
        ceiling = 0.0,
        externalRef = "",
        version = 0,
        status = RegionalCutoffClaimStatus.CREATED,
    )
}
