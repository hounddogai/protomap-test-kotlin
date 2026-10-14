package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive declaration segment records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a adaptive declaration segment is in its life cycle. */
enum class AdaptiveDeclarationSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive declaration segment. Its code is unique within a tenant. */
data class AdaptiveDeclarationSegment(
    var code: String,
    var name: String,
    var ownerId: String,
    var carrier: String,
    var attempts: Int,
    var ceiling: Double,
    var driverId: String,
    var summary: String,
    var windowDays: Int,
    var status: AdaptiveDeclarationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDeclarationSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDeclarationSegmentStatus): Boolean {
        when (status) {
            AdaptiveDeclarationSegmentStatus.OPEN -> {
                when (next) {
                    AdaptiveDeclarationSegmentStatus.IN_REVIEW -> return true
                }
                return false
            }
            AdaptiveDeclarationSegmentStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveDeclarationSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveDeclarationSegmentStatus.RESOLVED -> return next == AdaptiveDeclarationSegmentStatus.REOPENED || next == AdaptiveDeclarationSegmentStatus.ARCHIVED
            AdaptiveDeclarationSegmentStatus.REOPENED -> {
                when (next) {
                    AdaptiveDeclarationSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDeclarationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("adaptive declaration segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive declaration segment"} $code"

    fun netExposure(): Double = round((ceiling - windowDays.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDeclarationSegment {
        return AdaptiveDeclarationSegment(
            code = code,
            name = name,
            ownerId = ownerId,
            carrier = carrier,
            attempts = attempts,
            ceiling = ceiling,
            driverId = driverId,
            summary = summary,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive declaration segment in status open. */
fun newAdaptiveDeclarationSegment(code: String, name: String): AdaptiveDeclarationSegment {
    return AdaptiveDeclarationSegment(
        code = code,
        name = name,
        ownerId = "",
        carrier = "",
        attempts = 0,
        ceiling = 0.0,
        driverId = "",
        summary = "",
        windowDays = 0,
        status = AdaptiveDeclarationSegmentStatus.OPEN,
    )
}
