package com.acme.transport.customs.broker.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary broker entry records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a primary broker entry is in its life cycle. */
enum class PrimaryBrokerEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary broker entry. Its code is unique within a tenant. */
data class PrimaryBrokerEntry(
    var code: String,
    var name: String,
    var limit: Int,
    var priority: Int,
    var tags: MutableList<String>,
    var country: String,
    var ratio: Double,
    var status: PrimaryBrokerEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryBrokerEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryBrokerEntryStatus): Boolean {
        when (status) {
            PrimaryBrokerEntryStatus.CREATED -> return next == PrimaryBrokerEntryStatus.RETIRED
            PrimaryBrokerEntryStatus.VALIDATED -> return next == PrimaryBrokerEntryStatus.RETIRED
            PrimaryBrokerEntryStatus.PUBLISHED -> {
                when (next) {
                    PrimaryBrokerEntryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryBrokerEntryStatus) {
        if (!canMoveTo(next)) {
            error("primary broker entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((priority.toDouble() + ratio) * 100) / 100

    fun snapshot(): PrimaryBrokerEntry {
        return PrimaryBrokerEntry(
            code = code,
            name = name,
            limit = limit,
            priority = priority,
            tags = tags,
            country = country,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary broker entry in status created. */
fun newPrimaryBrokerEntry(code: String, name: String): PrimaryBrokerEntry {
    return PrimaryBrokerEntry(
        code = code,
        name = name,
        limit = 0,
        priority = 0,
        tags = mutableListOf<String>(),
        country = "",
        ratio = 0.0,
        status = PrimaryBrokerEntryStatus.CREATED,
    )
}
