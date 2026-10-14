package com.acme.transport.customs.clearance.policy

/** A rule a clearance rule must pass. Check returns its problems, or none. */
interface ClearanceRuleRule {
    fun check(item: ClearanceRule): MutableList<String>
}

data class ClearanceRuleCodePrefixRule(var prefix: String) : ClearanceRuleRule {
    override fun check(item: ClearanceRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRuleCodeLengthRule(var maxLength: Int) : ClearanceRuleRule {
    override fun check(item: ClearanceRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRuleFloorValueRangeRule(var min: Double, var max: Double) : ClearanceRuleRule {
    override fun check(item: ClearanceRule): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRuleDepotLengthRule(var maxLength: Int) : ClearanceRuleRule {
    override fun check(item: ClearanceRule): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceRuleNameLengthRule(var maxLength: Int) : ClearanceRuleRule {
    override fun check(item: ClearanceRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance rule. */
data class ClearanceRuleValidator(var rules: MutableList<ClearanceRuleRule>) {
    fun validate(item: ClearanceRule): MutableList<String> {
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

    fun requireValid(item: ClearanceRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceRule>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceRuleValidator(): ClearanceRuleValidator {
    return ClearanceRuleValidator(
        mutableListOf<ClearanceRuleRule>(
            ClearanceRuleCodePrefixRule("OPS"),
            ClearanceRuleCodeLengthRule(86400),
            ClearanceRuleFloorValueRangeRule(8.0, 1847.4),
            ClearanceRuleDepotLengthRule(86400),
            ClearanceRuleNameLengthRule(64),
        ),
    )
}
