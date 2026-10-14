package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.text.normalize

/** Where a global fuel burn digest is in its life cycle. */
enum class GlobalFuelBurnDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global fuel burn digest. Its code is unique within a tenant. */
data class GlobalFuelBurnDigest(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var description: String,
    var externalRef: String,
    var cost: Double,
    var active: Boolean,
    var trailerId: String,
    var archived: Boolean,
    var status: GlobalFuelBurnDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelBurnDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelBurnDigestStatus): Boolean {
        when (status) {
            GlobalFuelBurnDigestStatus.OPEN -> {
                when (next) {
                    GlobalFuelBurnDigestStatus.REOPENED -> return true
                }
                return false
            }
            GlobalFuelBurnDigestStatus.IN_REVIEW -> return next == GlobalFuelBurnDigestStatus.REOPENED || next == GlobalFuelBurnDigestStatus.ARCHIVED
            GlobalFuelBurnDigestStatus.RESOLVED -> {
                when (next) {
                    GlobalFuelBurnDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalFuelBurnDigestStatus.REOPENED -> {
                when (next) {
                    GlobalFuelBurnDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalFuelBurnDigestStatus) {
        if (!canMoveTo(next)) {
            error("global fuel burn digest $code cannot move from $status to $next")
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

    fun measuredValue(): Double = cost

    fun snapshot(): GlobalFuelBurnDigest {
        return GlobalFuelBurnDigest(
            code = code,
            label = label,
            tags = tags,
            description = description,
            externalRef = externalRef,
            cost = cost,
            active = active,
            trailerId = trailerId,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global fuel burn digest in status open. */
fun newGlobalFuelBurnDigest(code: String, label: String): GlobalFuelBurnDigest {
    return GlobalFuelBurnDigest(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        description = "",
        externalRef = "",
        cost = 0.0,
        active = false,
        trailerId = "",
        archived = false,
        status = GlobalFuelBurnDigestStatus.OPEN,
    )
}
