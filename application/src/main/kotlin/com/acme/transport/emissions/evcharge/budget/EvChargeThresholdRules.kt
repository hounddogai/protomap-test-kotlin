package com.acme.transport.emissions.evcharge.budget

/** A rule a ev charge threshold must pass. Check returns its problems, or none. */
interface EvChargeThresholdRule {
    fun check(item: EvChargeThreshold): MutableList<String>
}

data class EvChargeThresholdLabelLengthRule(var maxLength: Int) : EvChargeThresholdRule {
    override fun check(item: EvChargeThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeThresholdRequiredCodeRule(var message: String) : EvChargeThresholdRule {
    override fun check(item: EvChargeThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeThresholdCodeLengthRule(var maxLength: Int) : EvChargeThresholdRule {
    override fun check(item: EvChargeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeThresholdFloorValueRangeRule(var min: Double, var max: Double) : EvChargeThresholdRule {
    override fun check(item: EvChargeThreshold): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class EvChargeThresholdRequiredDescriptionRule(var message: String) : EvChargeThresholdRule {
    override fun check(item: EvChargeThreshold): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeThresholdUpdatedAtRangeRule(var min: Int, var max: Int) : EvChargeThresholdRule {
    override fun check(item: EvChargeThreshold): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a ev charge threshold. */
data class EvChargeThresholdValidator(var rules: MutableList<EvChargeThresholdRule>) {
    fun validate(item: EvChargeThreshold): MutableList<String> {
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

    fun requireValid(item: EvChargeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeThresholdValidator(): EvChargeThresholdValidator {
    return EvChargeThresholdValidator(
        mutableListOf<EvChargeThresholdRule>(
            EvChargeThresholdLabelLengthRule(120),
            EvChargeThresholdRequiredCodeRule("ev charge threshold needs a value"),
            EvChargeThresholdCodeLengthRule(255),
            EvChargeThresholdFloorValueRangeRule(1.4, 3381.4),
            EvChargeThresholdRequiredDescriptionRule("ev charge threshold needs a value"),
            EvChargeThresholdUpdatedAtRangeRule(32, 80),
        ),
    )
}
