package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary zone tier records of network's zone work, and the rules, stores, and calculations around them.

/** Where a primary zone tier is in its life cycle. */
enum class PrimaryZoneTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary zone tier. Its code is unique within a tenant. */
data class PrimaryZoneTier(
    var code: String,
    var title: String,
    var batchSize: Int,
    var weight: Double,
    var notes: String,
    var windowDays: Int,
    var externalRef: String,
    var limit: Int,
    var region: String,
    var rate: Double,
    var status: PrimaryZoneTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryZoneTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryZoneTierStatus): Boolean {
        when (status) {
            PrimaryZoneTierStatus.DRAFT -> {
                when (next) {
                    PrimaryZoneTierStatus.PENDING, PrimaryZoneTierStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryZoneTierStatus.PENDING -> {
                when (next) {
                    PrimaryZoneTierStatus.ACTIVE, PrimaryZoneTierStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryZoneTierStatus.ACTIVE -> return next == PrimaryZoneTierStatus.SUSPENDED || next == PrimaryZoneTierStatus.CLOSED
            PrimaryZoneTierStatus.SUSPENDED -> return next == PrimaryZoneTierStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryZoneTierStatus) {
        if (!canMoveTo(next)) {
            error("primary zone tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((limit.toDouble() + rate) * 100) / 100

    fun snapshot(): PrimaryZoneTier {
        return PrimaryZoneTier(
            code = code,
            title = title,
            batchSize = batchSize,
            weight = weight,
            notes = notes,
            windowDays = windowDays,
            externalRef = externalRef,
            limit = limit,
            region = region,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary zone tier in status draft. */
fun newPrimaryZoneTier(code: String, title: String): PrimaryZoneTier {
    return PrimaryZoneTier(
        code = code,
        title = title,
        batchSize = 0,
        weight = 0.0,
        notes = "",
        windowDays = 0,
        externalRef = "",
        limit = 0,
        region = "",
        rate = 0.0,
        status = PrimaryZoneTierStatus.DRAFT,
    )
}
