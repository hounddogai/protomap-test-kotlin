package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default hub entry is in its life cycle. */
enum class DefaultHubEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One default hub entry, with what the planners need to act on it. */
data class DefaultHubEntry(
    var code: String,
    var label: String,
    var approved: Boolean,
    var phone: String,
    var visible: Boolean,
    var channel: String,
    var trailerId: String,
    var retries: Int,
    var active: Boolean,
    var pallets: Int,
    var status: DefaultHubEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHubEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHubEntryStatus): Boolean {
        when (status) {
            DefaultHubEntryStatus.OPEN -> {
                when (next) {
                    DefaultHubEntryStatus.RESOLVED, DefaultHubEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultHubEntryStatus.IN_REVIEW -> {
                when (next) {
                    DefaultHubEntryStatus.REOPENED, DefaultHubEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultHubEntryStatus.RESOLVED -> {
                when (next) {
                    DefaultHubEntryStatus.REOPENED, DefaultHubEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultHubEntryStatus.REOPENED -> {
                when (next) {
                    DefaultHubEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultHubEntryStatus) {
        if (!canMoveTo(next)) {
            error("default hub entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default hub entry"} $code"

    fun projectedValue(): Double = round((pallets.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): DefaultHubEntry {
        return DefaultHubEntry(
            code = code,
            label = label,
            approved = approved,
            phone = phone,
            visible = visible,
            channel = channel,
            trailerId = trailerId,
            retries = retries,
            active = active,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default hub entry in status open. */
fun newDefaultHubEntry(code: String, label: String): DefaultHubEntry {
    return DefaultHubEntry(
        code = code,
        label = label,
        approved = false,
        phone = "",
        visible = false,
        channel = "",
        trailerId = "",
        retries = 0,
        active = false,
        pallets = 0,
        status = DefaultHubEntryStatus.OPEN,
    )
}
