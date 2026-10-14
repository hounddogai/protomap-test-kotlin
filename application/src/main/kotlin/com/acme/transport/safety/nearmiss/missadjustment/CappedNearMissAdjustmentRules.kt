package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.normalize

/** A rule a capped near miss adjustment must pass. Check returns its problems, or none. */
interface CappedNearMissAdjustmentRule {
    fun check(item: CappedNearMissAdjustment): MutableList<String>
}

data class CappedNearMissAdjustmentVersionRangeRule(var min: Int, var max: Int) : CappedNearMissAdjustmentRule {
    override fun check(item: CappedNearMissAdjustment): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class CappedNearMissAdjustmentLabelLengthRule(var maxLength: Int) : CappedNearMissAdjustmentRule {
    override fun check(item: CappedNearMissAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissAdjustmentFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : CappedNearMissAdjustmentRule {
    override fun check(item: CappedNearMissAdjustment): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissAdjustmentRequiredCodeRule(var message: String) : CappedNearMissAdjustmentRule {
    override fun check(item: CappedNearMissAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissAdjustmentRequiredLabelRule(var message: String) : CappedNearMissAdjustmentRule {
    override fun check(item: CappedNearMissAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNearMissAdjustmentAllowedLabelRule(
    var allowed: MutableList<String>,
) : CappedNearMissAdjustmentRule {
    override fun check(item: CappedNearMissAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped near miss adjustment. */
data class CappedNearMissAdjustmentValidator(var rules: MutableList<CappedNearMissAdjustmentRule>) {
    fun validate(item: CappedNearMissAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedNearMissAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedNearMissAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedNearMissAdjustmentValidator(): CappedNearMissAdjustmentValidator {
    return CappedNearMissAdjustmentValidator(
        mutableListOf<CappedNearMissAdjustmentRule>(
            CappedNearMissAdjustmentVersionRangeRule(255, 255),
            CappedNearMissAdjustmentLabelLengthRule(32),
            CappedNearMissAdjustmentFuelLitersRangeRule(7.7, 2246.7),
            CappedNearMissAdjustmentRequiredCodeRule("capped near miss adjustment needs a value"),
            CappedNearMissAdjustmentRequiredLabelRule("capped near miss adjustment needs a value"),
            CappedNearMissAdjustmentAllowedLabelRule(mutableListOf<String>("seasonal")),
        ),
    )
}
