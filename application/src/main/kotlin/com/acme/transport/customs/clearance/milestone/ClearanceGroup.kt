package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance group is in its life cycle. */
enum class ClearanceGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A clearance group as the clearance team keeps it. */
data class ClearanceGroup(
    var code: String,
    var name: String,
    var discount: Double,
    var description: String,
    var windowDays: Int,
    var externalRef: String,
    var score: Double,
    var status: ClearanceGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceGroupStatus): Boolean {
        when (status) {
            ClearanceGroupStatus.REQUESTED -> {
                when (next) {
                    ClearanceGroupStatus.GRANTED -> return true
                }
                return false
            }
            ClearanceGroupStatus.GRANTED -> {
                when (next) {
                    ClearanceGroupStatus.REVOKED, ClearanceGroupStatus.EXPIRED -> return true
                }
                return false
            }
            ClearanceGroupStatus.REVOKED -> {
                when (next) {
                    ClearanceGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceGroupStatus) {
        if (!canMoveTo(next)) {
            error("clearance group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round(discount * score * 100) / 100

    fun snapshot(): ClearanceGroup {
        return ClearanceGroup(
            code = code,
            name = name,
            discount = discount,
            description = description,
            windowDays = windowDays,
            externalRef = externalRef,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance group in status requested. */
fun newClearanceGroup(code: String, name: String): ClearanceGroup {
    return ClearanceGroup(
        code = code,
        name = name,
        discount = 0.0,
        description = "",
        windowDays = 0,
        externalRef = "",
        score = 0.0,
        status = ClearanceGroupStatus.REQUESTED,
    )
}
