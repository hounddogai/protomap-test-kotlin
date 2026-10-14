package com.acme.transport.linehaul.relay.target

/** A rule a tiered relay record must pass. Check returns its problems, or none. */
interface TieredRelayRecordRule {
    fun check(item: TieredRelayRecord): MutableList<String>
}

data class TieredRelayRecordSequenceRangeRule(var min: Int, var max: Int) : TieredRelayRecordRule {
    override fun check(item: TieredRelayRecord): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class TieredRelayRecordNameLengthRule(var maxLength: Int) : TieredRelayRecordRule {
    override fun check(item: TieredRelayRecord): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayRecordCodePrefixRule(var prefix: String) : TieredRelayRecordRule {
    override fun check(item: TieredRelayRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayRecordRequiredCodeRule(var message: String) : TieredRelayRecordRule {
    override fun check(item: TieredRelayRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered relay record. */
data class TieredRelayRecordValidator(var rules: MutableList<TieredRelayRecordRule>) {
    fun validate(item: TieredRelayRecord): MutableList<String> {
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

    fun requireValid(item: TieredRelayRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRelayRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRelayRecordValidator(): TieredRelayRecordValidator {
    return TieredRelayRecordValidator(
        mutableListOf<TieredRelayRecordRule>(
            TieredRelayRecordSequenceRangeRule(120, 120),
            TieredRelayRecordNameLengthRule(255),
            TieredRelayRecordCodePrefixRule("ACM"),
            TieredRelayRecordRequiredCodeRule("tiered relay record needs this field"),
        ),
    )
}
