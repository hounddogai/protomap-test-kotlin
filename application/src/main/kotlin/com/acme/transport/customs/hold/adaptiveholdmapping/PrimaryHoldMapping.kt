package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary hold mapping records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a primary hold mapping is in its life cycle. */
enum class PrimaryHoldMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary hold mapping, with what the planners need to act on it. */
data class PrimaryHoldMapping(
    var code: String,
    var label: String,
    var ownerId: String,
    var attributes: MutableMap<String, String>,
    var country: String,
    var distanceKm: Double,
    var attempts: Int,
    var discount: Double,
    var archived: Boolean,
    var reviewer: String,
    var status: PrimaryHoldMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryHoldMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryHoldMappingStatus): Boolean {
        when (status) {
            PrimaryHoldMappingStatus.NEW -> {
                when (next) {
                    PrimaryHoldMappingStatus.RUNNING -> return true
                }
                return false
            }
            PrimaryHoldMappingStatus.QUEUED -> return next == PrimaryHoldMappingStatus.DONE || next == PrimaryHoldMappingStatus.FAILED
            PrimaryHoldMappingStatus.RUNNING -> {
                when (next) {
                    PrimaryHoldMappingStatus.DONE, PrimaryHoldMappingStatus.FAILED -> return true
                }
                return false
            }
            PrimaryHoldMappingStatus.DONE -> {
                when (next) {
                    PrimaryHoldMappingStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryHoldMappingStatus) {
        if (!canMoveTo(next)) {
            error("primary hold mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round((distanceKm + attempts.toDouble()) * 100) / 100

    fun snapshot(): PrimaryHoldMapping {
        return PrimaryHoldMapping(
            code = code,
            label = label,
            ownerId = ownerId,
            attributes = attributes,
            country = country,
            distanceKm = distanceKm,
            attempts = attempts,
            discount = discount,
            archived = archived,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary hold mapping in status new. */
fun newPrimaryHoldMapping(code: String, label: String): PrimaryHoldMapping {
    return PrimaryHoldMapping(
        code = code,
        label = label,
        ownerId = "",
        attributes = mutableMapOf<String, String>(),
        country = "",
        distanceKm = 0.0,
        attempts = 0,
        discount = 0.0,
        archived = false,
        reviewer = "",
        status = PrimaryHoldMappingStatus.NEW,
    )
}
