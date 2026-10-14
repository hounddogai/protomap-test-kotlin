package com.acme.transport.lastmile.attempt.policy

/** A rule a attempt policy must pass. Check returns its problems, or none. */
interface AttemptPolicyRule {
    fun check(item: AttemptPolicy): MutableList<String>
}

data class AttemptPolicyCodePrefixRule(var prefix: String) : AttemptPolicyRule {
    override fun check(item: AttemptPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AttemptPolicyDurationMinutesRangeRule(var min: Int, var max: Int) : AttemptPolicyRule {
    override fun check(item: AttemptPolicy): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AttemptPolicyAmountRangeRule(var min: Double, var max: Double) : AttemptPolicyRule {
    override fun check(item: AttemptPolicy): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class AttemptPolicyRequiredCodeRule(var message: String) : AttemptPolicyRule {
    override fun check(item: AttemptPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt policy. */
data class AttemptPolicyValidator(var rules: MutableList<AttemptPolicyRule>) {
    fun validate(item: AttemptPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptPolicyValidator(): AttemptPolicyValidator {
    return AttemptPolicyValidator(
        mutableListOf<AttemptPolicyRule>(
            AttemptPolicyCodePrefixRule("Q"),
            AttemptPolicyDurationMinutesRangeRule(16, 3600),
            AttemptPolicyAmountRangeRule(2.7, 2910.8),
            AttemptPolicyRequiredCodeRule("attempt policy needs a value"),
        ),
    )
}
