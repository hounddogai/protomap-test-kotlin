package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional redelivery preference is in its life cycle. */
enum class RegionalRedeliveryPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional redelivery preference. Its code is unique within a tenant. */
data class RegionalRedeliveryPreference(
    var code: String,
    var name: String,
    var ratio: Double,
    var amount: Double,
    var endsAt: Int,
    var notes: String,
    var tags: MutableList<String>,
    var category: String,
    var loadKg: Double,
    var pallets: Int,
    var limit: Int,
    var status: RegionalRedeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRedeliveryPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRedeliveryPreferenceStatus): Boolean {
        when (status) {
            RegionalRedeliveryPreferenceStatus.NEW -> {
                when (next) {
                    RegionalRedeliveryPreferenceStatus.QUEUED, RegionalRedeliveryPreferenceStatus.DONE -> return true
                }
                return false
            }
            RegionalRedeliveryPreferenceStatus.QUEUED -> {
                when (next) {
                    RegionalRedeliveryPreferenceStatus.RUNNING -> return true
                }
                return false
            }
            RegionalRedeliveryPreferenceStatus.RUNNING -> return next == RegionalRedeliveryPreferenceStatus.FAILED
            RegionalRedeliveryPreferenceStatus.DONE -> return next == RegionalRedeliveryPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalRedeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("regional redelivery preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((pallets.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): RegionalRedeliveryPreference {
        return RegionalRedeliveryPreference(
            code = code,
            name = name,
            ratio = ratio,
            amount = amount,
            endsAt = endsAt,
            notes = notes,
            tags = tags,
            category = category,
            loadKg = loadKg,
            pallets = pallets,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional redelivery preference in status new. */
fun newRegionalRedeliveryPreference(code: String, name: String): RegionalRedeliveryPreference {
    return RegionalRedeliveryPreference(
        code = code,
        name = name,
        ratio = 0.0,
        amount = 0.0,
        endsAt = 0,
        notes = "",
        tags = mutableListOf<String>(),
        category = "",
        loadKg = 0.0,
        pallets = 0,
        limit = 0,
        status = RegionalRedeliveryPreferenceStatus.NEW,
    )
}
