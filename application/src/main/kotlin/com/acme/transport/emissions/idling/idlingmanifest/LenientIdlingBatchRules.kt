package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.text.normalize

/** A rule a lenient idling batch must pass. Check returns its problems, or none. */
interface LenientIdlingBatchRule {
    fun check(item: LenientIdlingBatch): MutableList<String>
}

data class LenientIdlingBatchPalletsRangeRule(var min: Int, var max: Int) : LenientIdlingBatchRule {
    override fun check(item: LenientIdlingBatch): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingBatchAllowedLabelRule(var allowed: MutableList<String>) : LenientIdlingBatchRule {
    override fun check(item: LenientIdlingBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingBatchRequiredCodeRule(var message: String) : LenientIdlingBatchRule {
    override fun check(item: LenientIdlingBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingBatchLabelLengthRule(var maxLength: Int) : LenientIdlingBatchRule {
    override fun check(item: LenientIdlingBatch): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingBatchRequiredUnitRule(var message: String) : LenientIdlingBatchRule {
    override fun check(item: LenientIdlingBatch): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient idling batch. */
data class LenientIdlingBatchValidator(var rules: MutableList<LenientIdlingBatchRule>) {
    fun validate(item: LenientIdlingBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientIdlingBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientIdlingBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientIdlingBatchValidator(): LenientIdlingBatchValidator {
    return LenientIdlingBatchValidator(
        mutableListOf<LenientIdlingBatchRule>(
            LenientIdlingBatchPalletsRangeRule(255, 1000),
            LenientIdlingBatchAllowedLabelRule(mutableListOf<String>("tiered", "lenient")),
            LenientIdlingBatchRequiredCodeRule("lenient idling batch needs this field"),
            LenientIdlingBatchLabelLengthRule(120),
            LenientIdlingBatchRequiredUnitRule("lenient idling batch needs this field"),
        ),
    )
}
