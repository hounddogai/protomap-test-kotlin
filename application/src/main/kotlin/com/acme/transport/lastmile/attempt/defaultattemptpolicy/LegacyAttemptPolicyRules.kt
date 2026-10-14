package com.acme.transport.lastmile.attempt.defaultattemptpolicy

/** A rule a legacy attempt policy must pass. Check returns its problems, or none. */
interface LegacyAttemptPolicyRule {
    fun check(item: LegacyAttemptPolicy): MutableList<String>
}

data class LegacyAttemptPolicyRequiredLabelRule(var message: String) : LegacyAttemptPolicyRule {
    override fun check(item: LegacyAttemptPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptPolicyRequiredCodeRule(var message: String) : LegacyAttemptPolicyRule {
    override fun check(item: LegacyAttemptPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptPolicyCeilingRangeRule(var min: Double, var max: Double) : LegacyAttemptPolicyRule {
    override fun check(item: LegacyAttemptPolicy): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class LegacyAttemptPolicyLabelLengthRule(var maxLength: Int) : LegacyAttemptPolicyRule {
    override fun check(item: LegacyAttemptPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAttemptPolicyCodeLengthRule(var maxLength: Int) : LegacyAttemptPolicyRule {
    override fun check(item: LegacyAttemptPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy attempt policy. */
data class LegacyAttemptPolicyValidator(var rules: MutableList<LegacyAttemptPolicyRule>) {
    fun validate(item: LegacyAttemptPolicy): MutableList<String> {
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

    fun requireValid(item: LegacyAttemptPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAttemptPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAttemptPolicyValidator(): LegacyAttemptPolicyValidator {
    return LegacyAttemptPolicyValidator(
        mutableListOf<LegacyAttemptPolicyRule>(
            LegacyAttemptPolicyRequiredLabelRule("legacy attempt policy needs this field"),
            LegacyAttemptPolicyRequiredCodeRule("legacy attempt policy needs an entry"),
            LegacyAttemptPolicyCeilingRangeRule(4.7, 2267.2),
            LegacyAttemptPolicyLabelLengthRule(32),
            LegacyAttemptPolicyCodeLengthRule(86400),
        ),
    )
}
