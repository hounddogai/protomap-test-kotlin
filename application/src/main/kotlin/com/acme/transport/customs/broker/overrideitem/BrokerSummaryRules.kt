package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a broker summary must pass. Check returns its problems, or none. */
interface BrokerSummaryRule {
    fun check(item: BrokerSummary): MutableList<String>
}

data class BrokerSummaryRequiredCountryRule(var message: String) : BrokerSummaryRule {
    override fun check(item: BrokerSummary): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerSummaryPhoneLengthRule(var maxLength: Int) : BrokerSummaryRule {
    override fun check(item: BrokerSummary): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerSummaryQuantityRangeRule(var min: Int, var max: Int) : BrokerSummaryRule {
    override fun check(item: BrokerSummary): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerSummarySequenceRangeRule(var min: Int, var max: Int) : BrokerSummaryRule {
    override fun check(item: BrokerSummary): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerSummaryAllowedCodeRule(var allowed: MutableList<String>) : BrokerSummaryRule {
    override fun check(item: BrokerSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker summary. */
data class BrokerSummaryValidator(var rules: MutableList<BrokerSummaryRule>) {
    fun validate(item: BrokerSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerSummaryValidator(): BrokerSummaryValidator {
    return BrokerSummaryValidator(
        mutableListOf<BrokerSummaryRule>(
            BrokerSummaryRequiredCountryRule("broker summary needs a value"),
            BrokerSummaryPhoneLengthRule(3600),
            BrokerSummaryQuantityRangeRule(64, 120),
            BrokerSummarySequenceRangeRule(255, 86400),
            BrokerSummaryAllowedCodeRule(mutableListOf<String>("weighted")),
        ),
    )
}
