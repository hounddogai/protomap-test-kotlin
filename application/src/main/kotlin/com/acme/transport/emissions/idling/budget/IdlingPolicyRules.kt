package com.acme.transport.emissions.idling.budget

/** A rule a idling policy must pass. Check returns its problems, or none. */
interface IdlingPolicyRule {
    fun check(item: IdlingPolicy): MutableList<String>
}

data class IdlingPolicyUnitLengthRule(var maxLength: Int) : IdlingPolicyRule {
    override fun check(item: IdlingPolicy): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingPolicyCeilingRangeRule(var min: Double, var max: Double) : IdlingPolicyRule {
    override fun check(item: IdlingPolicy): MutableList<String> {
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

data class IdlingPolicyContactEmailLengthRule(var maxLength: Int) : IdlingPolicyRule {
    override fun check(item: IdlingPolicy): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingPolicyCodeLengthRule(var maxLength: Int) : IdlingPolicyRule {
    override fun check(item: IdlingPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingPolicyRequiredTitleRule(var message: String) : IdlingPolicyRule {
    override fun check(item: IdlingPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingPolicyRequiredUnitRule(var message: String) : IdlingPolicyRule {
    override fun check(item: IdlingPolicy): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling policy. */
data class IdlingPolicyValidator(var rules: MutableList<IdlingPolicyRule>) {
    fun validate(item: IdlingPolicy): MutableList<String> {
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

    fun requireValid(item: IdlingPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingPolicyValidator(): IdlingPolicyValidator {
    return IdlingPolicyValidator(
        mutableListOf<IdlingPolicyRule>(
            IdlingPolicyUnitLengthRule(120),
            IdlingPolicyCeilingRangeRule(7.2, 179.8),
            IdlingPolicyContactEmailLengthRule(16),
            IdlingPolicyCodeLengthRule(16),
            IdlingPolicyRequiredTitleRule("idling policy needs this field"),
            IdlingPolicyRequiredUnitRule("idling policy needs an entry"),
        ),
    )
}
