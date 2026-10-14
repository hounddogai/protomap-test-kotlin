package com.acme.transport.network.transittime.ledger

/** A rule a default transit time policy must pass. Check returns its problems, or none. */
interface DefaultTransitTimePolicyRule {
    fun check(item: DefaultTransitTimePolicy): MutableList<String>
}

data class DefaultTransitTimePolicyCodePrefixRule(var prefix: String) : DefaultTransitTimePolicyRule {
    override fun check(item: DefaultTransitTimePolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimePolicyCodeLengthRule(var maxLength: Int) : DefaultTransitTimePolicyRule {
    override fun check(item: DefaultTransitTimePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimePolicyRequiredLabelRule(var message: String) : DefaultTransitTimePolicyRule {
    override fun check(item: DefaultTransitTimePolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default transit time policy. */
data class DefaultTransitTimePolicyValidator(var rules: MutableList<DefaultTransitTimePolicyRule>) {
    fun validate(item: DefaultTransitTimePolicy): MutableList<String> {
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

    fun requireValid(item: DefaultTransitTimePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTransitTimePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTransitTimePolicyValidator(): DefaultTransitTimePolicyValidator {
    return DefaultTransitTimePolicyValidator(
        mutableListOf<DefaultTransitTimePolicyRule>(
            DefaultTransitTimePolicyCodePrefixRule("Q"),
            DefaultTransitTimePolicyCodeLengthRule(86400),
            DefaultTransitTimePolicyRequiredLabelRule("default transit time policy needs a value"),
        ),
    )
}
