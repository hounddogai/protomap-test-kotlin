package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation manifest records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation manifest is in its life cycle. */
enum class ViolationManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a violation manifest and its current status. */
data class ViolationManifest(
    var code: String,
    var name: String,
    var attempts: Int,
    var sequence: Int,
    var maximum: Double,
    var status: ViolationManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationManifestStatus): Boolean {
        when (status) {
            ViolationManifestStatus.REQUESTED -> return next == ViolationManifestStatus.GRANTED
            ViolationManifestStatus.GRANTED -> {
                when (next) {
                    ViolationManifestStatus.REVOKED -> return true
                }
                return false
            }
            ViolationManifestStatus.REVOKED -> return next == ViolationManifestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ViolationManifestStatus) {
        if (!canMoveTo(next)) {
            error("violation manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"violation manifest"} $code"

    fun adjustedValue(): Double = round(sequence.toDouble() * maximum * 100) / 100

    fun snapshot(): ViolationManifest {
        return ViolationManifest(
            code = code,
            name = name,
            attempts = attempts,
            sequence = sequence,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation manifest in status requested. */
fun newViolationManifest(code: String, name: String): ViolationManifest {
    return ViolationManifest(
        code = code,
        name = name,
        attempts = 0,
        sequence = 0,
        maximum = 0.0,
        status = ViolationManifestStatus.REQUESTED,
    )
}
