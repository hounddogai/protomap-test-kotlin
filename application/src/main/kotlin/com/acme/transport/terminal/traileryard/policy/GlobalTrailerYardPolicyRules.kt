package com.acme.transport.terminal.traileryard.policy

/** A rule a global trailer yard policy must pass. Check returns its problems, or none. */
interface GlobalTrailerYardPolicyRule {
    fun check(item: GlobalTrailerYardPolicy): MutableList<String>
}

data class GlobalTrailerYardPolicyCodePrefixRule(var prefix: String) : GlobalTrailerYardPolicyRule {
    override fun check(item: GlobalTrailerYardPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardPolicyCurrencyLengthRule(var maxLength: Int) : GlobalTrailerYardPolicyRule {
    override fun check(item: GlobalTrailerYardPolicy): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardPolicyDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalTrailerYardPolicyRule {
    override fun check(item: GlobalTrailerYardPolicy): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalTrailerYardPolicyRequiredCurrencyRule(var message: String) : GlobalTrailerYardPolicyRule {
    override fun check(item: GlobalTrailerYardPolicy): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerYardPolicyCodeLengthRule(var maxLength: Int) : GlobalTrailerYardPolicyRule {
    override fun check(item: GlobalTrailerYardPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer yard policy. */
data class GlobalTrailerYardPolicyValidator(var rules: MutableList<GlobalTrailerYardPolicyRule>) {
    fun validate(item: GlobalTrailerYardPolicy): MutableList<String> {
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

    fun requireValid(item: GlobalTrailerYardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerYardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerYardPolicyValidator(): GlobalTrailerYardPolicyValidator {
    return GlobalTrailerYardPolicyValidator(
        mutableListOf<GlobalTrailerYardPolicyRule>(
            GlobalTrailerYardPolicyCodePrefixRule("Q"),
            GlobalTrailerYardPolicyCurrencyLengthRule(32),
            GlobalTrailerYardPolicyDurationMinutesRangeRule(16, 32),
            GlobalTrailerYardPolicyRequiredCurrencyRule("global trailer yard policy needs a value"),
            GlobalTrailerYardPolicyCodeLengthRule(64),
        ),
    )
}
