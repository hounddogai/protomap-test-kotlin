package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.normalize

/** A rule a relay record must pass. Check returns its problems, or none. */
interface RelayRecordRule {
    fun check(item: RelayRecord): MutableList<String>
}

data class RelayRecordThresholdRangeRule(var min: Double, var max: Double) : RelayRecordRule {
    override fun check(item: RelayRecord): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class RelayRecordCodeLengthRule(var maxLength: Int) : RelayRecordRule {
    override fun check(item: RelayRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayRecordLimitRangeRule(var min: Int, var max: Int) : RelayRecordRule {
    override fun check(item: RelayRecord): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class RelayRecordCodePrefixRule(var prefix: String) : RelayRecordRule {
    override fun check(item: RelayRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayRecordAllowedTitleRule(var allowed: MutableList<String>) : RelayRecordRule {
    override fun check(item: RelayRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay record. */
data class RelayRecordValidator(var rules: MutableList<RelayRecordRule>) {
    fun validate(item: RelayRecord): MutableList<String> {
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

    fun requireValid(item: RelayRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayRecordValidator(): RelayRecordValidator {
    return RelayRecordValidator(
        mutableListOf<RelayRecordRule>(
            RelayRecordThresholdRangeRule(0.9, 3713.1),
            RelayRecordCodeLengthRule(80),
            RelayRecordLimitRangeRule(32, 3600),
            RelayRecordCodePrefixRule("ACM"),
            RelayRecordAllowedTitleRule(mutableListOf<String>("global", "default")),
        ),
    )
}
