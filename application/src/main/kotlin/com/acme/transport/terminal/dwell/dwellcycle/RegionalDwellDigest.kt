package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.text.normalize

/** Where a regional dwell digest is in its life cycle. */
enum class RegionalDwellDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional dwell digest. Its code is unique within a tenant. */
data class RegionalDwellDigest(
    var code: String,
    var label: String,
    var reviewer: String,
    var tags: MutableList<String>,
    var amount: Double,
    var active: Boolean,
    var status: RegionalDwellDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDwellDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDwellDigestStatus): Boolean {
        when (status) {
            RegionalDwellDigestStatus.REQUESTED -> return next == RegionalDwellDigestStatus.REVOKED
            RegionalDwellDigestStatus.GRANTED -> {
                when (next) {
                    RegionalDwellDigestStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalDwellDigestStatus.REVOKED -> return next == RegionalDwellDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalDwellDigestStatus) {
        if (!canMoveTo(next)) {
            error("regional dwell digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = amount

    fun snapshot(): RegionalDwellDigest {
        return RegionalDwellDigest(
            code = code,
            label = label,
            reviewer = reviewer,
            tags = tags,
            amount = amount,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional dwell digest in status requested. */
fun newRegionalDwellDigest(code: String, label: String): RegionalDwellDigest {
    return RegionalDwellDigest(
        code = code,
        label = label,
        reviewer = "",
        tags = mutableListOf<String>(),
        amount = 0.0,
        active = false,
        status = RegionalDwellDigestStatus.REQUESTED,
    )
}
