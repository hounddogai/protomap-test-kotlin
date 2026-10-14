package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation bundle is in its life cycle. */
enum class ViolationBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a violation bundle and its current status. */
data class ViolationBundle(
    var code: String,
    var name: String,
    var score: Double,
    var checksum: String,
    var budget: Double,
    var status: ViolationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationBundleStatus): Boolean {
        when (status) {
            ViolationBundleStatus.REQUESTED -> {
                when (next) {
                    ViolationBundleStatus.GRANTED, ViolationBundleStatus.EXPIRED -> return true
                }
                return false
            }
            ViolationBundleStatus.GRANTED -> {
                when (next) {
                    ViolationBundleStatus.REVOKED -> return true
                }
                return false
            }
            ViolationBundleStatus.REVOKED -> {
                when (next) {
                    ViolationBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationBundleStatus) {
        if (!canMoveTo(next)) {
            error("violation bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round(budget * score * 100) / 100

    fun snapshot(): ViolationBundle {
        return ViolationBundle(
            code = code,
            name = name,
            score = score,
            checksum = checksum,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation bundle in status requested. */
fun newViolationBundle(code: String, name: String): ViolationBundle {
    return ViolationBundle(
        code = code,
        name = name,
        score = 0.0,
        checksum = "",
        budget = 0.0,
        status = ViolationBundleStatus.REQUESTED,
    )
}
