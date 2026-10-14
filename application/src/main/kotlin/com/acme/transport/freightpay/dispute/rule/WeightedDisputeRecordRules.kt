package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.kit.text.normalize

/** A rule a weighted dispute record must pass. Check returns its problems, or none. */
interface WeightedDisputeRecordRule {
    fun check(item: WeightedDisputeRecord): MutableList<String>
}

data class WeightedDisputeRecordRequiredLabelRule(var message: String) : WeightedDisputeRecordRule {
    override fun check(item: WeightedDisputeRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeRecordRequiredCodeRule(var message: String) : WeightedDisputeRecordRule {
    override fun check(item: WeightedDisputeRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeRecordAllowedCodeRule(var allowed: MutableList<String>) : WeightedDisputeRecordRule {
    override fun check(item: WeightedDisputeRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeRecordLabelLengthRule(var maxLength: Int) : WeightedDisputeRecordRule {
    override fun check(item: WeightedDisputeRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeRecordRetriesRangeRule(var min: Int, var max: Int) : WeightedDisputeRecordRule {
    override fun check(item: WeightedDisputeRecord): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDisputeRecordRequiredDescriptionRule(var message: String) : WeightedDisputeRecordRule {
    override fun check(item: WeightedDisputeRecord): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted dispute record. */
data class WeightedDisputeRecordValidator(var rules: MutableList<WeightedDisputeRecordRule>) {
    fun validate(item: WeightedDisputeRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedDisputeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDisputeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDisputeRecordValidator(): WeightedDisputeRecordValidator {
    return WeightedDisputeRecordValidator(
        mutableListOf<WeightedDisputeRecordRule>(
            WeightedDisputeRecordRequiredLabelRule("weighted dispute record needs this field"),
            WeightedDisputeRecordRequiredCodeRule("weighted dispute record needs an entry"),
            WeightedDisputeRecordAllowedCodeRule(mutableListOf<String>("scheduled", "seasonal", "strict")),
            WeightedDisputeRecordLabelLengthRule(86400),
            WeightedDisputeRecordRetriesRangeRule(255, 3600),
            WeightedDisputeRecordRequiredDescriptionRule("weighted dispute record needs an entry"),
        ),
    )
}
