package com.acme.transport.customs.broker.group

/** A rule a default broker override must pass. Check returns its problems, or none. */
interface DefaultBrokerOverrideRule {
    fun check(item: DefaultBrokerOverride): MutableList<String>
}

data class DefaultBrokerOverrideRevisionRangeRule(var min: Int, var max: Int) : DefaultBrokerOverrideRule {
    override fun check(item: DefaultBrokerOverride): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class DefaultBrokerOverrideWindowRule(var maxSpan: Int) : DefaultBrokerOverrideRule {
    override fun check(item: DefaultBrokerOverride): MutableList<String> {
        if (item.startsAt > item.endsAt) {
            return mutableListOf<String>("starts at is after ends at")
        }
        if (maxSpan > 0 && item.endsAt - item.startsAt > maxSpan) {
            return mutableListOf<String>("spans more than $maxSpan seconds")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBrokerOverrideCodePrefixRule(var prefix: String) : DefaultBrokerOverrideRule {
    override fun check(item: DefaultBrokerOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default broker override. */
data class DefaultBrokerOverrideValidator(var rules: MutableList<DefaultBrokerOverrideRule>) {
    fun validate(item: DefaultBrokerOverride): MutableList<String> {
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

    fun requireValid(item: DefaultBrokerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultBrokerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultBrokerOverrideValidator(): DefaultBrokerOverrideValidator {
    return DefaultBrokerOverrideValidator(
        mutableListOf<DefaultBrokerOverrideRule>(
            DefaultBrokerOverrideRevisionRangeRule(32, 1000),
            DefaultBrokerOverrideWindowRule(32),
            DefaultBrokerOverrideCodePrefixRule("X"),
        ),
    )
}
