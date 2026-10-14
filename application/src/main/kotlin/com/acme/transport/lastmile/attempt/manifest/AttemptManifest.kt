package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.text.normalize

// The attempt manifest records of lastmile's attempt work, and the rules, stores, and calculations around
// them.

/** Where a attempt manifest is in its life cycle. */
enum class AttemptManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One attempt manifest, with what the planners need to act on it. */
data class AttemptManifest(
    var code: String,
    var name: String,
    var unit: String,
    var margin: Double,
    var shiftCode: String,
    var status: AttemptManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptManifestStatus): Boolean {
        when (status) {
            AttemptManifestStatus.OPEN -> return next == AttemptManifestStatus.RESOLVED
            AttemptManifestStatus.IN_REVIEW -> return next == AttemptManifestStatus.RESOLVED || next == AttemptManifestStatus.ARCHIVED
            AttemptManifestStatus.RESOLVED -> {
                when (next) {
                    AttemptManifestStatus.REOPENED, AttemptManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            AttemptManifestStatus.REOPENED -> {
                when (next) {
                    AttemptManifestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptManifestStatus) {
        if (!canMoveTo(next)) {
            error("attempt manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"attempt manifest"} $code"

    fun measuredValue(): Double = margin

    fun snapshot(): AttemptManifest {
        return AttemptManifest(
            code = code,
            name = name,
            unit = unit,
            margin = margin,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a attempt manifest in status open. */
fun newAttemptManifest(code: String, name: String): AttemptManifest {
    return AttemptManifest(
        code = code,
        name = name,
        unit = "",
        margin = 0.0,
        shiftCode = "",
        status = AttemptManifestStatus.OPEN,
    )
}
