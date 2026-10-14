package com.acme.transport.emissions.evcharge.chargescore

/** A rule a ev charge cycle must pass. Check returns its problems, or none. */
interface EvChargeCycleRule {
    fun check(item: EvChargeCycle): MutableList<String>
}

data class EvChargeCycleExpiresAtRangeRule(var min: Int, var max: Int) : EvChargeCycleRule {
    override fun check(item: EvChargeCycle): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class EvChargeCycleRequiredLabelRule(var message: String) : EvChargeCycleRule {
    override fun check(item: EvChargeCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeCycleCodeLengthRule(var maxLength: Int) : EvChargeCycleRule {
    override fun check(item: EvChargeCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeCycleLimitRangeRule(var min: Int, var max: Int) : EvChargeCycleRule {
    override fun check(item: EvChargeCycle): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class EvChargeCycleFloorValueRangeRule(var min: Double, var max: Double) : EvChargeCycleRule {
    override fun check(item: EvChargeCycle): MutableList<String> {
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

data class EvChargeCycleRequiredCodeRule(var message: String) : EvChargeCycleRule {
    override fun check(item: EvChargeCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge cycle. */
data class EvChargeCycleValidator(var rules: MutableList<EvChargeCycleRule>) {
    fun validate(item: EvChargeCycle): MutableList<String> {
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

    fun requireValid(item: EvChargeCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeCycleValidator(): EvChargeCycleValidator {
    return EvChargeCycleValidator(
        mutableListOf<EvChargeCycleRule>(
            EvChargeCycleExpiresAtRangeRule(255, 86400),
            EvChargeCycleRequiredLabelRule("ev charge cycle needs this field"),
            EvChargeCycleCodeLengthRule(120),
            EvChargeCycleLimitRangeRule(1000, 86400),
            EvChargeCycleFloorValueRangeRule(8.5, 2332.0),
            EvChargeCycleRequiredCodeRule("ev charge cycle needs a value"),
        ),
    )
}
