package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub entry is in its life cycle. */
enum class HubEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A hub entry as the hub team keeps it. */
data class HubEntry(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var retries: Int,
    var endsAt: Int,
    var status: HubEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubEntryStatus): Boolean {
        when (status) {
            HubEntryStatus.NEW -> return next == HubEntryStatus.DONE || next == HubEntryStatus.FAILED
            HubEntryStatus.QUEUED -> {
                when (next) {
                    HubEntryStatus.RUNNING -> return true
                }
                return false
            }
            HubEntryStatus.RUNNING -> {
                when (next) {
                    HubEntryStatus.DONE, HubEntryStatus.FAILED -> return true
                }
                return false
            }
            HubEntryStatus.DONE -> {
                when (next) {
                    HubEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubEntryStatus) {
        if (!canMoveTo(next)) {
            error("hub entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub entry"} $code"

    fun weightedLoad(): Double = round(etaMinutes.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): HubEntry {
        return HubEntry(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            retries = retries,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hub entry in status new. */
fun newHubEntry(code: String, name: String): HubEntry {
    return HubEntry(
        code = code,
        name = name,
        etaMinutes = 0,
        retries = 0,
        endsAt = 0,
        status = HubEntryStatus.NEW,
    )
}
