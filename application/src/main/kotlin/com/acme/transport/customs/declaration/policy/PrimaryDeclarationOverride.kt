package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.text.normalize

/** Where a primary declaration override is in its life cycle. */
enum class PrimaryDeclarationOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One primary declaration override, with what the planners need to act on it. */
data class PrimaryDeclarationOverride(
    var code: String,
    var title: String,
    var segment: String,
    var source: String,
    var startsAt: Int,
    var status: PrimaryDeclarationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDeclarationOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDeclarationOverrideStatus): Boolean {
        when (status) {
            PrimaryDeclarationOverrideStatus.PLANNED -> return next == PrimaryDeclarationOverrideStatus.SCHEDULED
            PrimaryDeclarationOverrideStatus.SCHEDULED -> {
                when (next) {
                    PrimaryDeclarationOverrideStatus.COMPLETED, PrimaryDeclarationOverrideStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryDeclarationOverrideStatus.IN_PROGRESS -> return next == PrimaryDeclarationOverrideStatus.CANCELLED
            PrimaryDeclarationOverrideStatus.COMPLETED -> return next == PrimaryDeclarationOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PrimaryDeclarationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary declaration override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): PrimaryDeclarationOverride {
        return PrimaryDeclarationOverride(
            code = code,
            title = title,
            segment = segment,
            source = source,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary declaration override in status planned. */
fun newPrimaryDeclarationOverride(code: String, title: String): PrimaryDeclarationOverride {
    return PrimaryDeclarationOverride(
        code = code,
        title = title,
        segment = "",
        source = "",
        startsAt = 0,
        status = PrimaryDeclarationOverrideStatus.PLANNED,
    )
}
