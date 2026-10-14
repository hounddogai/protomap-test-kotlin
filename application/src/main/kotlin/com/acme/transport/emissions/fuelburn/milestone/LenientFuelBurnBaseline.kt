package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient fuel burn baseline is in its life cycle. */
enum class LenientFuelBurnBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient fuel burn baseline and its current status. */
data class LenientFuelBurnBaseline(
    var code: String,
    var name: String,
    var batchSize: Int,
    var stopCount: Int,
    var attempts: Int,
    var version: Int,
    var etaMinutes: Int,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var visible: Boolean,
    var status: LenientFuelBurnBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelBurnBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelBurnBaselineStatus): Boolean {
        when (status) {
            LenientFuelBurnBaselineStatus.CREATED -> return next == LenientFuelBurnBaselineStatus.RETIRED
            LenientFuelBurnBaselineStatus.VALIDATED -> return next == LenientFuelBurnBaselineStatus.PUBLISHED || next == LenientFuelBurnBaselineStatus.RETIRED
            LenientFuelBurnBaselineStatus.PUBLISHED -> {
                when (next) {
                    LenientFuelBurnBaselineStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientFuelBurnBaselineStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel burn baseline $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(attempts.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): LenientFuelBurnBaseline {
        return LenientFuelBurnBaseline(
            code = code,
            name = name,
            batchSize = batchSize,
            stopCount = stopCount,
            attempts = attempts,
            version = version,
            etaMinutes = etaMinutes,
            cost = cost,
            attributes = attributes,
            active = active,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient fuel burn baseline in status created. */
fun newLenientFuelBurnBaseline(code: String, name: String): LenientFuelBurnBaseline {
    return LenientFuelBurnBaseline(
        code = code,
        name = name,
        batchSize = 0,
        stopCount = 0,
        attempts = 0,
        version = 0,
        etaMinutes = 0,
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        active = false,
        visible = false,
        status = LenientFuelBurnBaselineStatus.CREATED,
    )
}
