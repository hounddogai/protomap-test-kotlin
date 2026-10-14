package com.acme.transport.linehaul.laneplan.cycle

/** A rule a lane plan mapping must pass. Check returns its problems, or none. */
interface LanePlanMappingRule {
    fun check(item: LanePlanMapping): MutableList<String>
}

data class LanePlanMappingBudgetRangeRule(var min: Double, var max: Double) : LanePlanMappingRule {
    override fun check(item: LanePlanMapping): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class LanePlanMappingOdometerRangeRule(var min: Int, var max: Int) : LanePlanMappingRule {
    override fun check(item: LanePlanMapping): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanMappingCodeLengthRule(var maxLength: Int) : LanePlanMappingRule {
    override fun check(item: LanePlanMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanMappingRevisionRangeRule(var min: Int, var max: Int) : LanePlanMappingRule {
    override fun check(item: LanePlanMapping): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanMappingCodePrefixRule(var prefix: String) : LanePlanMappingRule {
    override fun check(item: LanePlanMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanMappingLabelLengthRule(var maxLength: Int) : LanePlanMappingRule {
    override fun check(item: LanePlanMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan mapping. */
data class LanePlanMappingValidator(var rules: MutableList<LanePlanMappingRule>) {
    fun validate(item: LanePlanMapping): MutableList<String> {
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

    fun requireValid(item: LanePlanMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanMappingValidator(): LanePlanMappingValidator {
    return LanePlanMappingValidator(
        mutableListOf<LanePlanMappingRule>(
            LanePlanMappingBudgetRangeRule(4.3, 3395.4),
            LanePlanMappingOdometerRangeRule(16, 64),
            LanePlanMappingCodeLengthRule(64),
            LanePlanMappingRevisionRangeRule(16, 255),
            LanePlanMappingCodePrefixRule("REF"),
            LanePlanMappingLabelLengthRule(80),
        ),
    )
}
