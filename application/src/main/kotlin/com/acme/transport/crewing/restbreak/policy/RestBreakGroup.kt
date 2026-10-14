package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break group is in its life cycle. */
enum class RestBreakGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rest break group and its current status. */
data class RestBreakGroup(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var budget: Double,
    var phone: String,
    var timezone: String,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var title: String,
    var status: RestBreakGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakGroupStatus): Boolean {
        when (status) {
            RestBreakGroupStatus.REQUESTED -> return next == RestBreakGroupStatus.GRANTED
            RestBreakGroupStatus.GRANTED -> return next == RestBreakGroupStatus.REVOKED || next == RestBreakGroupStatus.EXPIRED
            RestBreakGroupStatus.REVOKED -> return next == RestBreakGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RestBreakGroupStatus) {
        if (!canMoveTo(next)) {
            error("rest break group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rest break group"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round((budget + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakGroup {
        return RestBreakGroup(
            code = code,
            label = label,
            updatedAt = updatedAt,
            budget = budget,
            phone = phone,
            timezone = timezone,
            odometer = odometer,
            attributes = attributes,
            sequence = sequence,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rest break group in status requested. */
fun newRestBreakGroup(code: String, label: String): RestBreakGroup {
    return RestBreakGroup(
        code = code,
        label = label,
        updatedAt = 0,
        budget = 0.0,
        phone = "",
        timezone = "",
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        title = "",
        status = RestBreakGroupStatus.REQUESTED,
    )
}
