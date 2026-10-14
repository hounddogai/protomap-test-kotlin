package com.acme.transport.customs.broker.overrideitem

/** A rule a broker override must pass. Check returns its problems, or none. */
interface BrokerOverrideRule {
    fun check(item: BrokerOverride): MutableList<String>
}

data class BrokerOverrideTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BrokerOverrideRule {
    override fun check(item: BrokerOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class BrokerOverrideCurrencyLengthRule(var maxLength: Int) : BrokerOverrideRule {
    override fun check(item: BrokerOverride): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerOverrideCodeLengthRule(var maxLength: Int) : BrokerOverrideRule {
    override fun check(item: BrokerOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerOverrideQuantityRangeRule(var min: Int, var max: Int) : BrokerOverrideRule {
    override fun check(item: BrokerOverride): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker override. */
data class BrokerOverrideValidator(var rules: MutableList<BrokerOverrideRule>) {
    fun validate(item: BrokerOverride): MutableList<String> {
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

    fun requireValid(item: BrokerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerOverrideValidator(): BrokerOverrideValidator {
    return BrokerOverrideValidator(
        mutableListOf<BrokerOverrideRule>(
            BrokerOverrideTagsLimitRule(64, mutableListOf<String>("weighted")),
            BrokerOverrideCurrencyLengthRule(1000),
            BrokerOverrideCodeLengthRule(64),
            BrokerOverrideQuantityRangeRule(32, 86400),
        ),
    )
}
