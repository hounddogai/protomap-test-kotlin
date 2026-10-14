package com.acme.transport.terminal.dockdoor.claim

/** A rule a lenient dock door policy must pass. Check returns its problems, or none. */
interface LenientDockDoorPolicyRule {
    fun check(item: LenientDockDoorPolicy): MutableList<String>
}

data class LenientDockDoorPolicyRequiredSummaryRule(var message: String) : LenientDockDoorPolicyRule {
    override fun check(item: LenientDockDoorPolicy): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDockDoorPolicyPriorityRangeRule(var min: Int, var max: Int) : LenientDockDoorPolicyRule {
    override fun check(item: LenientDockDoorPolicy): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class LenientDockDoorPolicySummaryLengthRule(var maxLength: Int) : LenientDockDoorPolicyRule {
    override fun check(item: LenientDockDoorPolicy): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dock door policy. */
data class LenientDockDoorPolicyValidator(var rules: MutableList<LenientDockDoorPolicyRule>) {
    fun validate(item: LenientDockDoorPolicy): MutableList<String> {
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

    fun requireValid(item: LenientDockDoorPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDockDoorPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDockDoorPolicyValidator(): LenientDockDoorPolicyValidator {
    return LenientDockDoorPolicyValidator(
        mutableListOf<LenientDockDoorPolicyRule>(
            LenientDockDoorPolicyRequiredSummaryRule("lenient dock door policy needs a value"),
            LenientDockDoorPolicyPriorityRangeRule(80, 255),
            LenientDockDoorPolicySummaryLengthRule(80),
        ),
    )
}
