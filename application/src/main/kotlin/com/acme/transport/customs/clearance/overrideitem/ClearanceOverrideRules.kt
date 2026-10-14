package com.acme.transport.customs.clearance.overrideitem

/** A rule a clearance override must pass. Check returns its problems, or none. */
interface ClearanceOverrideRule {
    fun check(item: ClearanceOverride): MutableList<String>
}

data class ClearanceOverrideRatioRangeRule(var min: Double, var max: Double) : ClearanceOverrideRule {
    override fun check(item: ClearanceOverride): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class ClearanceOverrideRequiredCodeRule(var message: String) : ClearanceOverrideRule {
    override fun check(item: ClearanceOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceOverrideNameLengthRule(var maxLength: Int) : ClearanceOverrideRule {
    override fun check(item: ClearanceOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance override. */
data class ClearanceOverrideValidator(var rules: MutableList<ClearanceOverrideRule>) {
    fun validate(item: ClearanceOverride): MutableList<String> {
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

    fun requireValid(item: ClearanceOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceOverrideValidator(): ClearanceOverrideValidator {
    return ClearanceOverrideValidator(
        mutableListOf<ClearanceOverrideRule>(
            ClearanceOverrideRatioRangeRule(9.8, 1196.4),
            ClearanceOverrideRequiredCodeRule("clearance override needs this field"),
            ClearanceOverrideNameLengthRule(255),
        ),
    )
}
