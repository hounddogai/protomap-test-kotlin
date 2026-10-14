package com.acme.transport.network.hub.batch

/** A rule a hub batch must pass. Check returns its problems, or none. */
interface HubBatchRule {
    fun check(item: HubBatch): MutableList<String>
}

data class HubBatchRequiredCodeRule(var message: String) : HubBatchRule {
    override fun check(item: HubBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubBatchCodePrefixRule(var prefix: String) : HubBatchRule {
    override fun check(item: HubBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubBatchAmountRangeRule(var min: Double, var max: Double) : HubBatchRule {
    override fun check(item: HubBatch): MutableList<String> {
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

data class HubBatchRequiredChannelRule(var message: String) : HubBatchRule {
    override fun check(item: HubBatch): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubBatchLoadKgRangeRule(var min: Double, var max: Double) : HubBatchRule {
    override fun check(item: HubBatch): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubBatchRequiredNameRule(var message: String) : HubBatchRule {
    override fun check(item: HubBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub batch. */
data class HubBatchValidator(var rules: MutableList<HubBatchRule>) {
    fun validate(item: HubBatch): MutableList<String> {
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

    fun requireValid(item: HubBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultHubBatchValidator(): HubBatchValidator {
    return HubBatchValidator(
        mutableListOf<HubBatchRule>(
            HubBatchRequiredCodeRule("hub batch needs a value"),
            HubBatchCodePrefixRule("X"),
            HubBatchAmountRangeRule(8.9, 4597.8),
            HubBatchRequiredChannelRule("hub batch needs a value"),
            HubBatchLoadKgRangeRule(0.5, 1796.2),
            HubBatchRequiredNameRule("hub batch needs this field"),
        ),
    )
}
