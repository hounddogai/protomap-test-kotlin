package com.acme.transport.network.zone.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped zone group records of network's zone work, and the rules, stores, and calculations around them.

/** Where a capped zone group is in its life cycle. */
enum class CappedZoneGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped zone group. Its code is unique within a tenant. */
data class CappedZoneGroup(
    var code: String,
    var name: String,
    var retries: Int,
    var pallets: Int,
    var dock: String,
    var endsAt: Int,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var maximum: Double,
    var cost: Double,
    var startsAt: Int,
    var status: CappedZoneGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedZoneGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedZoneGroupStatus): Boolean {
        when (status) {
            CappedZoneGroupStatus.NEW -> return next == CappedZoneGroupStatus.QUEUED || next == CappedZoneGroupStatus.FAILED
            CappedZoneGroupStatus.QUEUED -> {
                when (next) {
                    CappedZoneGroupStatus.FAILED -> return true
                }
                return false
            }
            CappedZoneGroupStatus.RUNNING -> return next == CappedZoneGroupStatus.FAILED
            CappedZoneGroupStatus.DONE -> {
                when (next) {
                    CappedZoneGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedZoneGroupStatus) {
        if (!canMoveTo(next)) {
            error("capped zone group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun adjustedValue(): Double = round((maximum + retries.toDouble()) * 100) / 100

    fun snapshot(): CappedZoneGroup {
        return CappedZoneGroup(
            code = code,
            name = name,
            retries = retries,
            pallets = pallets,
            dock = dock,
            endsAt = endsAt,
            distanceKm = distanceKm,
            tags = tags,
            maximum = maximum,
            cost = cost,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped zone group in status new. */
fun newCappedZoneGroup(code: String, name: String): CappedZoneGroup {
    return CappedZoneGroup(
        code = code,
        name = name,
        retries = 0,
        pallets = 0,
        dock = "",
        endsAt = 0,
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        maximum = 0.0,
        cost = 0.0,
        startsAt = 0,
        status = CappedZoneGroupStatus.NEW,
    )
}
