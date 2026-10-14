package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.text.normalize

/** A rule a broker entry must pass. Check returns its problems, or none. */
interface BrokerEntryRule {
    fun check(item: BrokerEntry): MutableList<String>
}

data class BrokerEntryOdometerRangeRule(var min: Int, var max: Int) : BrokerEntryRule {
    override fun check(item: BrokerEntry): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerEntryActiveGuardRule(var limit: Int) : BrokerEntryRule {
    override fun check(item: BrokerEntry): MutableList<String> {
        if (item.active && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BrokerEntryCurrencyLengthRule(var maxLength: Int) : BrokerEntryRule {
    override fun check(item: BrokerEntry): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerEntryAllowedNameRule(var allowed: MutableList<String>) : BrokerEntryRule {
    override fun check(item: BrokerEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker entry. */
data class BrokerEntryValidator(var rules: MutableList<BrokerEntryRule>) {
    fun validate(item: BrokerEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerEntryValidator(): BrokerEntryValidator {
    return BrokerEntryValidator(
        mutableListOf<BrokerEntryRule>(
            BrokerEntryOdometerRangeRule(32, 64),
            BrokerEntryActiveGuardRule(64),
            BrokerEntryCurrencyLengthRule(80),
            BrokerEntryAllowedNameRule(mutableListOf<String>("seasonal", "legacy")),
        ),
    )
}
