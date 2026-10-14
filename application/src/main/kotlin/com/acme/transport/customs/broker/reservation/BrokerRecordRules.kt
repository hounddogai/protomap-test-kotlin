package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.text.normalize

/** A rule a broker record must pass. Check returns its problems, or none. */
interface BrokerRecordRule {
    fun check(item: BrokerRecord): MutableList<String>
}

data class BrokerRecordRequiredTitleRule(var message: String) : BrokerRecordRule {
    override fun check(item: BrokerRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerRecordRequiredCodeRule(var message: String) : BrokerRecordRule {
    override fun check(item: BrokerRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerRecordCodeLengthRule(var maxLength: Int) : BrokerRecordRule {
    override fun check(item: BrokerRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerRecordAllowedCodeRule(var allowed: MutableList<String>) : BrokerRecordRule {
    override fun check(item: BrokerRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BrokerRecordQuantityRangeRule(var min: Int, var max: Int) : BrokerRecordRule {
    override fun check(item: BrokerRecord): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker record. */
data class BrokerRecordValidator(var rules: MutableList<BrokerRecordRule>) {
    fun validate(item: BrokerRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerRecordValidator(): BrokerRecordValidator {
    return BrokerRecordValidator(
        mutableListOf<BrokerRecordRule>(
            BrokerRecordRequiredTitleRule("broker record needs a value"),
            BrokerRecordRequiredCodeRule("broker record needs a value"),
            BrokerRecordCodeLengthRule(32),
            BrokerRecordAllowedCodeRule(mutableListOf<String>()),
            BrokerRecordQuantityRangeRule(16, 255),
        ),
    )
}
