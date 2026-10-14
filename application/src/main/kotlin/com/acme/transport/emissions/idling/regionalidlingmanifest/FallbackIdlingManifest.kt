package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback idling manifest records of emissions's idling work, and the rules, stores, and calculations
// around them.

/** Where a fallback idling manifest is in its life cycle. */
enum class FallbackIdlingManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback idling manifest, with what the planners need to act on it. */
data class FallbackIdlingManifest(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var contactEmail: String,
    var quantity: Int,
    var name: String,
    var priority: Int,
    var floorValue: Double,
    var country: String,
    var endsAt: Int,
    var locale: String,
    var status: FallbackIdlingManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackIdlingManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackIdlingManifestStatus): Boolean {
        when (status) {
            FallbackIdlingManifestStatus.DRAFT -> return next == FallbackIdlingManifestStatus.ACTIVE || next == FallbackIdlingManifestStatus.CLOSED
            FallbackIdlingManifestStatus.PENDING -> return next == FallbackIdlingManifestStatus.SUSPENDED || next == FallbackIdlingManifestStatus.CLOSED
            FallbackIdlingManifestStatus.ACTIVE -> return next == FallbackIdlingManifestStatus.SUSPENDED || next == FallbackIdlingManifestStatus.CLOSED
            FallbackIdlingManifestStatus.SUSPENDED -> {
                when (next) {
                    FallbackIdlingManifestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackIdlingManifestStatus) {
        if (!canMoveTo(next)) {
            error("fallback idling manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback idling manifest"} $code"

    fun effectiveValue(): Double = round(endsAt.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): FallbackIdlingManifest {
        return FallbackIdlingManifest(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            contactEmail = contactEmail,
            quantity = quantity,
            name = name,
            priority = priority,
            floorValue = floorValue,
            country = country,
            endsAt = endsAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback idling manifest in status draft. */
fun newFallbackIdlingManifest(code: String, title: String): FallbackIdlingManifest {
    return FallbackIdlingManifest(
        code = code,
        title = title,
        fuelLiters = 0.0,
        contactEmail = "",
        quantity = 0,
        name = "",
        priority = 0,
        floorValue = 0.0,
        country = "",
        endsAt = 0,
        locale = "",
        status = FallbackIdlingManifestStatus.DRAFT,
    )
}
