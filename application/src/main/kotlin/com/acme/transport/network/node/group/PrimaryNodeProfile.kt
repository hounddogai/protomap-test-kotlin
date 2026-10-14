package com.acme.transport.network.node.group

import com.acme.transport.kit.text.normalize

/** Where a primary node profile is in its life cycle. */
enum class PrimaryNodeProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary node profile as the node team keeps it. */
data class PrimaryNodeProfile(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var budget: Double,
    var channel: String,
    var country: String,
    var status: PrimaryNodeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNodeProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNodeProfileStatus): Boolean {
        when (status) {
            PrimaryNodeProfileStatus.DRAFT -> return next == PrimaryNodeProfileStatus.ACTIVE || next == PrimaryNodeProfileStatus.SUSPENDED
            PrimaryNodeProfileStatus.PENDING -> {
                when (next) {
                    PrimaryNodeProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            PrimaryNodeProfileStatus.ACTIVE -> return next == PrimaryNodeProfileStatus.SUSPENDED
            PrimaryNodeProfileStatus.SUSPENDED -> {
                when (next) {
                    PrimaryNodeProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryNodeProfileStatus) {
        if (!canMoveTo(next)) {
            error("primary node profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = budget

    fun snapshot(): PrimaryNodeProfile {
        return PrimaryNodeProfile(
            code = code,
            name = name,
            tags = tags,
            budget = budget,
            channel = channel,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary node profile in status draft. */
fun newPrimaryNodeProfile(code: String, name: String): PrimaryNodeProfile {
    return PrimaryNodeProfile(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        budget = 0.0,
        channel = "",
        country = "",
        status = PrimaryNodeProfileStatus.DRAFT,
    )
}
