package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional sortation claim is in its life cycle. */
enum class RegionalSortationClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional sortation claim as the sortation team keeps it. */
data class RegionalSortationClaim(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var shiftCode: String,
    var graceDays: Int,
    var contactEmail: String,
    var category: String,
    var expiresAt: Int,
    var active: Boolean,
    var title: String,
    var summary: String,
    var status: RegionalSortationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalSortationClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalSortationClaimStatus): Boolean {
        when (status) {
            RegionalSortationClaimStatus.DRAFT -> {
                when (next) {
                    RegionalSortationClaimStatus.ACTIVE, RegionalSortationClaimStatus.CLOSED -> return true
                }
                return false
            }
            RegionalSortationClaimStatus.PENDING -> {
                when (next) {
                    RegionalSortationClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalSortationClaimStatus.ACTIVE -> {
                when (next) {
                    RegionalSortationClaimStatus.SUSPENDED, RegionalSortationClaimStatus.CLOSED -> return true
                }
                return false
            }
            RegionalSortationClaimStatus.SUSPENDED -> return next == RegionalSortationClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalSortationClaimStatus) {
        if (!canMoveTo(next)) {
            error("regional sortation claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((graceDays.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalSortationClaim {
        return RegionalSortationClaim(
            code = code,
            name = name,
            tags = tags,
            shiftCode = shiftCode,
            graceDays = graceDays,
            contactEmail = contactEmail,
            category = category,
            expiresAt = expiresAt,
            active = active,
            title = title,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional sortation claim in status draft. */
fun newRegionalSortationClaim(code: String, name: String): RegionalSortationClaim {
    return RegionalSortationClaim(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        shiftCode = "",
        graceDays = 0,
        contactEmail = "",
        category = "",
        expiresAt = 0,
        active = false,
        title = "",
        summary = "",
        status = RegionalSortationClaimStatus.DRAFT,
    )
}
