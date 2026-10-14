package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.text.normalize

/** A rule a tender checkpoint must pass. Check returns its problems, or none. */
interface TenderCheckpointRule {
    fun check(item: TenderCheckpoint): MutableList<String>
}

data class TenderCheckpointFuelLitersRangeRule(var min: Double, var max: Double) : TenderCheckpointRule {
    override fun check(item: TenderCheckpoint): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TenderCheckpointWindowDaysRangeRule(var min: Int, var max: Int) : TenderCheckpointRule {
    override fun check(item: TenderCheckpoint): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class TenderCheckpointVehicleIdLengthRule(var maxLength: Int) : TenderCheckpointRule {
    override fun check(item: TenderCheckpoint): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderCheckpointAllowedCodeRule(var allowed: MutableList<String>) : TenderCheckpointRule {
    override fun check(item: TenderCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TenderCheckpointRequiredCurrencyRule(var message: String) : TenderCheckpointRule {
    override fun check(item: TenderCheckpoint): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderCheckpointFloorValueRangeRule(var min: Double, var max: Double) : TenderCheckpointRule {
    override fun check(item: TenderCheckpoint): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender checkpoint. */
data class TenderCheckpointValidator(var rules: MutableList<TenderCheckpointRule>) {
    fun validate(item: TenderCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: TenderCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderCheckpoint>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultTenderCheckpointValidator(): TenderCheckpointValidator {
    return TenderCheckpointValidator(
        mutableListOf<TenderCheckpointRule>(
            TenderCheckpointFuelLitersRangeRule(9.0, 3104.4),
            TenderCheckpointWindowDaysRangeRule(16, 64),
            TenderCheckpointVehicleIdLengthRule(3600),
            TenderCheckpointAllowedCodeRule(mutableListOf<String>()),
            TenderCheckpointRequiredCurrencyRule("tender checkpoint needs a value"),
            TenderCheckpointFloorValueRangeRule(8.4, 2422.1),
        ),
    )
}
