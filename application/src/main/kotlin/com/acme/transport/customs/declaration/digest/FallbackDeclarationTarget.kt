package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback declaration target is in its life cycle. */
enum class FallbackDeclarationTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a fallback declaration target and its current status. */
data class FallbackDeclarationTarget(
    var code: String,
    var title: String,
    var windowDays: Int,
    var trailerId: String,
    var createdAt: Int,
    var region: String,
    var discount: Double,
    var notes: String,
    var pinned: Boolean,
    var attributes: MutableMap<String, String>,
    var status: FallbackDeclarationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDeclarationTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDeclarationTargetStatus): Boolean {
        when (status) {
            FallbackDeclarationTargetStatus.PROPOSED -> {
                when (next) {
                    FallbackDeclarationTargetStatus.APPROVED, FallbackDeclarationTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackDeclarationTargetStatus.APPROVED -> {
                when (next) {
                    FallbackDeclarationTargetStatus.REJECTED -> return true
                }
                return false
            }
            FallbackDeclarationTargetStatus.REJECTED -> return next == FallbackDeclarationTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackDeclarationTargetStatus) {
        if (!canMoveTo(next)) {
            error("fallback declaration target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((discount + windowDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackDeclarationTarget {
        return FallbackDeclarationTarget(
            code = code,
            title = title,
            windowDays = windowDays,
            trailerId = trailerId,
            createdAt = createdAt,
            region = region,
            discount = discount,
            notes = notes,
            pinned = pinned,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback declaration target in status proposed. */
fun newFallbackDeclarationTarget(code: String, title: String): FallbackDeclarationTarget {
    return FallbackDeclarationTarget(
        code = code,
        title = title,
        windowDays = 0,
        trailerId = "",
        createdAt = 0,
        region = "",
        discount = 0.0,
        notes = "",
        pinned = false,
        attributes = mutableMapOf<String, String>(),
        status = FallbackDeclarationTargetStatus.PROPOSED,
    )
}
