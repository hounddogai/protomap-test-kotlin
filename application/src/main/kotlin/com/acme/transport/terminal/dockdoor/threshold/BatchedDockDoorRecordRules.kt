package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.text.normalize

/** A rule a batched dock door record must pass. Check returns its problems, or none. */
interface BatchedDockDoorRecordRule {
    fun check(item: BatchedDockDoorRecord): MutableList<String>
}

data class BatchedDockDoorRecordCountryLengthRule(var maxLength: Int) : BatchedDockDoorRecordRule {
    override fun check(item: BatchedDockDoorRecord): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorRecordRequiredLabelRule(var message: String) : BatchedDockDoorRecordRule {
    override fun check(item: BatchedDockDoorRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorRecordMarginRangeRule(var min: Double, var max: Double) : BatchedDockDoorRecordRule {
    override fun check(item: BatchedDockDoorRecord): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class BatchedDockDoorRecordAllowedCountryRule(
    var allowed: MutableList<String>,
) : BatchedDockDoorRecordRule {
    override fun check(item: BatchedDockDoorRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorRecordActiveGuardRule(var limit: Double) : BatchedDockDoorRecordRule {
    override fun check(item: BatchedDockDoorRecord): MutableList<String> {
        if (item.active && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorRecordCodeLengthRule(var maxLength: Int) : BatchedDockDoorRecordRule {
    override fun check(item: BatchedDockDoorRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dock door record. */
data class BatchedDockDoorRecordValidator(var rules: MutableList<BatchedDockDoorRecordRule>) {
    fun validate(item: BatchedDockDoorRecord): MutableList<String> {
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

    fun requireValid(item: BatchedDockDoorRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDockDoorRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDockDoorRecordValidator(): BatchedDockDoorRecordValidator {
    return BatchedDockDoorRecordValidator(
        mutableListOf<BatchedDockDoorRecordRule>(
            BatchedDockDoorRecordCountryLengthRule(255),
            BatchedDockDoorRecordRequiredLabelRule("batched dock door record needs an entry"),
            BatchedDockDoorRecordMarginRangeRule(5.9, 1740.9),
            BatchedDockDoorRecordAllowedCountryRule(mutableListOf<String>()),
            BatchedDockDoorRecordActiveGuardRule(5.6),
            BatchedDockDoorRecordCodeLengthRule(32),
        ),
    )
}
