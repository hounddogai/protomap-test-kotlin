package com.acme.transport.customs.broker.digest

import com.acme.transport.kit.text.normalize

/** A rule a weighted broker record must pass. Check returns its problems, or none. */
interface WeightedBrokerRecordRule {
    fun check(item: WeightedBrokerRecord): MutableList<String>
}

data class WeightedBrokerRecordAllowedNameRule(var allowed: MutableList<String>) : WeightedBrokerRecordRule {
    override fun check(item: WeightedBrokerRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerRecordLoadKgRangeRule(var min: Double, var max: Double) : WeightedBrokerRecordRule {
    override fun check(item: WeightedBrokerRecord): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerRecordCodePrefixRule(var prefix: String) : WeightedBrokerRecordRule {
    override fun check(item: WeightedBrokerRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerRecordRequiredNameRule(var message: String) : WeightedBrokerRecordRule {
    override fun check(item: WeightedBrokerRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerRecordDepotLengthRule(var maxLength: Int) : WeightedBrokerRecordRule {
    override fun check(item: WeightedBrokerRecord): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted broker record. */
data class WeightedBrokerRecordValidator(var rules: MutableList<WeightedBrokerRecordRule>) {
    fun validate(item: WeightedBrokerRecord): MutableList<String> {
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

    fun requireValid(item: WeightedBrokerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedBrokerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedBrokerRecordValidator(): WeightedBrokerRecordValidator {
    return WeightedBrokerRecordValidator(
        mutableListOf<WeightedBrokerRecordRule>(
            WeightedBrokerRecordAllowedNameRule(mutableListOf<String>()),
            WeightedBrokerRecordLoadKgRangeRule(7.7, 575.1),
            WeightedBrokerRecordCodePrefixRule("X"),
            WeightedBrokerRecordRequiredNameRule("weighted broker record needs this field"),
            WeightedBrokerRecordDepotLengthRule(120),
        ),
    )
}
