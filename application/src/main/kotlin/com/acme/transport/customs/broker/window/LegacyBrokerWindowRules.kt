package com.acme.transport.customs.broker.window

/** A rule a legacy broker window must pass. Check returns its problems, or none. */
interface LegacyBrokerWindowRule {
    fun check(item: LegacyBrokerWindow): MutableList<String>
}

data class LegacyBrokerWindowRouteCodeLengthRule(var maxLength: Int) : LegacyBrokerWindowRule {
    override fun check(item: LegacyBrokerWindow): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerWindowCodeLengthRule(var maxLength: Int) : LegacyBrokerWindowRule {
    override fun check(item: LegacyBrokerWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerWindowStartsAtRangeRule(var min: Int, var max: Int) : LegacyBrokerWindowRule {
    override fun check(item: LegacyBrokerWindow): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerWindowCodePrefixRule(var prefix: String) : LegacyBrokerWindowRule {
    override fun check(item: LegacyBrokerWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy broker window. */
data class LegacyBrokerWindowValidator(var rules: MutableList<LegacyBrokerWindowRule>) {
    fun validate(item: LegacyBrokerWindow): MutableList<String> {
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

    fun requireValid(item: LegacyBrokerWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBrokerWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBrokerWindowValidator(): LegacyBrokerWindowValidator {
    return LegacyBrokerWindowValidator(
        mutableListOf<LegacyBrokerWindowRule>(
            LegacyBrokerWindowRouteCodeLengthRule(120),
            LegacyBrokerWindowCodeLengthRule(1000),
            LegacyBrokerWindowStartsAtRangeRule(64, 255),
            LegacyBrokerWindowCodePrefixRule("X"),
        ),
    )
}
