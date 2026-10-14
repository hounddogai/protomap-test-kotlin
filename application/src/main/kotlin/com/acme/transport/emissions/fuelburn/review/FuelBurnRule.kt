package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn rule is in its life cycle. */
enum class FuelBurnRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel burn rule and its current status. */
data class FuelBurnRule(
    var code: String,
    var name: String,
    var archived: Boolean,
    var minimum: Double,
    var threshold: Double,
    var revision: Int,
    var notes: String,
    var phone: String,
    var weight: Double,
    var locale: String,
    var stopCount: Int,
    var status: FuelBurnRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnRuleStatus): Boolean {
        when (status) {
            FuelBurnRuleStatus.PLANNED -> return next == FuelBurnRuleStatus.SCHEDULED
            FuelBurnRuleStatus.SCHEDULED -> return next == FuelBurnRuleStatus.CANCELLED
            FuelBurnRuleStatus.IN_PROGRESS -> return next == FuelBurnRuleStatus.COMPLETED || next == FuelBurnRuleStatus.CANCELLED
            FuelBurnRuleStatus.COMPLETED -> return next == FuelBurnRuleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FuelBurnRuleStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round((stopCount.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnRule {
        return FuelBurnRule(
            code = code,
            name = name,
            archived = archived,
            minimum = minimum,
            threshold = threshold,
            revision = revision,
            notes = notes,
            phone = phone,
            weight = weight,
            locale = locale,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fuel burn rule in status planned. */
fun newFuelBurnRule(code: String, name: String): FuelBurnRule {
    return FuelBurnRule(
        code = code,
        name = name,
        archived = false,
        minimum = 0.0,
        threshold = 0.0,
        revision = 0,
        notes = "",
        phone = "",
        weight = 0.0,
        locale = "",
        stopCount = 0,
        status = FuelBurnRuleStatus.PLANNED,
    )
}
