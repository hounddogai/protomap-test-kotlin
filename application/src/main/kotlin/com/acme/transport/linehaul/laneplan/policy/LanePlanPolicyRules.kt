package com.acme.transport.linehaul.laneplan.policy

/** A rule a lane plan policy must pass. Check returns its problems, or none. */
interface LanePlanPolicyRule {
    fun check(item: LanePlanPolicy): MutableList<String>
}

data class LanePlanPolicyMinimumRangeRule(var min: Double, var max: Double) : LanePlanPolicyRule {
    override fun check(item: LanePlanPolicy): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanPolicyRequiredCodeRule(var message: String) : LanePlanPolicyRule {
    override fun check(item: LanePlanPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanPolicyMarginRangeRule(var min: Double, var max: Double) : LanePlanPolicyRule {
    override fun check(item: LanePlanPolicy): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanPolicyCodePrefixRule(var prefix: String) : LanePlanPolicyRule {
    override fun check(item: LanePlanPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanPolicyRequiredTimezoneRule(var message: String) : LanePlanPolicyRule {
    override fun check(item: LanePlanPolicy): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanPolicyCodeLengthRule(var maxLength: Int) : LanePlanPolicyRule {
    override fun check(item: LanePlanPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan policy. */
data class LanePlanPolicyValidator(var rules: MutableList<LanePlanPolicyRule>) {
    fun validate(item: LanePlanPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanPolicyValidator(): LanePlanPolicyValidator {
    return LanePlanPolicyValidator(
        mutableListOf<LanePlanPolicyRule>(
            LanePlanPolicyMinimumRangeRule(9.5, 3113.5),
            LanePlanPolicyRequiredCodeRule("lane plan policy needs this field"),
            LanePlanPolicyMarginRangeRule(3.2, 2785.3),
            LanePlanPolicyCodePrefixRule("X"),
            LanePlanPolicyRequiredTimezoneRule("lane plan policy needs an entry"),
            LanePlanPolicyCodeLengthRule(80),
        ),
    )
}
