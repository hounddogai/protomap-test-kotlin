package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling declaration tier is in its life cycle. */
enum class RollingDeclarationTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a rolling declaration tier and its current status. */
data class RollingDeclarationTier(
    var code: String,
    var name: String,
    var description: String,
    var budget: Double,
    var graceDays: Int,
    var source: String,
    var weight: Double,
    var distanceKm: Double,
    var unit: String,
    var status: RollingDeclarationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDeclarationTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDeclarationTierStatus): Boolean {
        when (status) {
            RollingDeclarationTierStatus.NEW -> {
                when (next) {
                    RollingDeclarationTierStatus.RUNNING, RollingDeclarationTierStatus.DONE -> return true
                }
                return false
            }
            RollingDeclarationTierStatus.QUEUED -> return next == RollingDeclarationTierStatus.RUNNING || next == RollingDeclarationTierStatus.FAILED
            RollingDeclarationTierStatus.RUNNING -> return next == RollingDeclarationTierStatus.FAILED
            RollingDeclarationTierStatus.DONE -> return next == RollingDeclarationTierStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingDeclarationTierStatus) {
        if (!canMoveTo(next)) {
            error("rolling declaration tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling declaration tier"} $code"

    fun weightedValue(): Double = round(distanceKm * weight * 100) / 100

    fun snapshot(): RollingDeclarationTier {
        return RollingDeclarationTier(
            code = code,
            name = name,
            description = description,
            budget = budget,
            graceDays = graceDays,
            source = source,
            weight = weight,
            distanceKm = distanceKm,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling declaration tier in status new. */
fun newRollingDeclarationTier(code: String, name: String): RollingDeclarationTier {
    return RollingDeclarationTier(
        code = code,
        name = name,
        description = "",
        budget = 0.0,
        graceDays = 0,
        source = "",
        weight = 0.0,
        distanceKm = 0.0,
        unit = "",
        status = RollingDeclarationTierStatus.NEW,
    )
}
