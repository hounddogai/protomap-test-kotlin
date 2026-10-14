package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.kit.text.normalize

/** A rule a adaptive broker batch must pass. Check returns its problems, or none. */
interface AdaptiveBrokerBatchRule {
    fun check(item: AdaptiveBrokerBatch): MutableList<String>
}

data class AdaptiveBrokerBatchAllowedShiftCodeRule(var allowed: MutableList<String>) : AdaptiveBrokerBatchRule {
    override fun check(item: AdaptiveBrokerBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerBatchStartsAtRangeRule(var min: Int, var max: Int) : AdaptiveBrokerBatchRule {
    override fun check(item: AdaptiveBrokerBatch): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerBatchPhoneLengthRule(var maxLength: Int) : AdaptiveBrokerBatchRule {
    override fun check(item: AdaptiveBrokerBatch): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerBatchRequiredTitleRule(var message: String) : AdaptiveBrokerBatchRule {
    override fun check(item: AdaptiveBrokerBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive broker batch. */
data class AdaptiveBrokerBatchValidator(var rules: MutableList<AdaptiveBrokerBatchRule>) {
    fun validate(item: AdaptiveBrokerBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveBrokerBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBrokerBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBrokerBatchValidator(): AdaptiveBrokerBatchValidator {
    return AdaptiveBrokerBatchValidator(
        mutableListOf<AdaptiveBrokerBatchRule>(
            AdaptiveBrokerBatchAllowedShiftCodeRule(mutableListOf<String>("pending")),
            AdaptiveBrokerBatchStartsAtRangeRule(16, 3600),
            AdaptiveBrokerBatchPhoneLengthRule(32),
            AdaptiveBrokerBatchRequiredTitleRule("adaptive broker batch needs a value"),
        ),
    )
}
