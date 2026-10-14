package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.text.normalize

/** A rule a default relay tier must pass. Check returns its problems, or none. */
interface DefaultRelayTierRule {
    fun check(item: DefaultRelayTier): MutableList<String>
}

data class DefaultRelayTierBatchSizeRangeRule(var min: Int, var max: Int) : DefaultRelayTierRule {
    override fun check(item: DefaultRelayTier): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class DefaultRelayTierRequiredSummaryRule(var message: String) : DefaultRelayTierRule {
    override fun check(item: DefaultRelayTier): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayTierNameLengthRule(var maxLength: Int) : DefaultRelayTierRule {
    override fun check(item: DefaultRelayTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayTierAllowedSummaryRule(var allowed: MutableList<String>) : DefaultRelayTierRule {
    override fun check(item: DefaultRelayTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayTierRequiredNameRule(var message: String) : DefaultRelayTierRule {
    override fun check(item: DefaultRelayTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayTierWindowDaysRangeRule(var min: Int, var max: Int) : DefaultRelayTierRule {
    override fun check(item: DefaultRelayTier): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default relay tier. */
data class DefaultRelayTierValidator(var rules: MutableList<DefaultRelayTierRule>) {
    fun validate(item: DefaultRelayTier): MutableList<String> {
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

    fun requireValid(item: DefaultRelayTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRelayTier>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRelayTierValidator(): DefaultRelayTierValidator {
    return DefaultRelayTierValidator(
        mutableListOf<DefaultRelayTierRule>(
            DefaultRelayTierBatchSizeRangeRule(16, 1000),
            DefaultRelayTierRequiredSummaryRule("default relay tier needs an entry"),
            DefaultRelayTierNameLengthRule(1000),
            DefaultRelayTierAllowedSummaryRule(mutableListOf<String>("capped")),
            DefaultRelayTierRequiredNameRule("default relay tier needs this field"),
            DefaultRelayTierWindowDaysRangeRule(32, 1000),
        ),
    )
}
