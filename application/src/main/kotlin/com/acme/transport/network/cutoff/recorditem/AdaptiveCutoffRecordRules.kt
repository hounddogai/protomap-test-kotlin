package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a adaptive cutoff record must pass. Check returns its problems, or none. */
interface AdaptiveCutoffRecordRule {
    fun check(item: AdaptiveCutoffRecord): MutableList<String>
}

data class AdaptiveCutoffRecordCountryLengthRule(var maxLength: Int) : AdaptiveCutoffRecordRule {
    override fun check(item: AdaptiveCutoffRecord): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCutoffRecordDescriptionLengthRule(var maxLength: Int) : AdaptiveCutoffRecordRule {
    override fun check(item: AdaptiveCutoffRecord): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCutoffRecordLimitRangeRule(var min: Int, var max: Int) : AdaptiveCutoffRecordRule {
    override fun check(item: AdaptiveCutoffRecord): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCutoffRecordWeightRangeRule(var min: Double, var max: Double) : AdaptiveCutoffRecordRule {
    override fun check(item: AdaptiveCutoffRecord): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCutoffRecordCapacityRangeRule(var min: Int, var max: Int) : AdaptiveCutoffRecordRule {
    override fun check(item: AdaptiveCutoffRecord): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCutoffRecordAllowedCodeRule(var allowed: MutableList<String>) : AdaptiveCutoffRecordRule {
    override fun check(item: AdaptiveCutoffRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive cutoff record. */
data class AdaptiveCutoffRecordValidator(var rules: MutableList<AdaptiveCutoffRecordRule>) {
    fun validate(item: AdaptiveCutoffRecord): MutableList<String> {
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

    fun requireValid(item: AdaptiveCutoffRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCutoffRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCutoffRecordValidator(): AdaptiveCutoffRecordValidator {
    return AdaptiveCutoffRecordValidator(
        mutableListOf<AdaptiveCutoffRecordRule>(
            AdaptiveCutoffRecordCountryLengthRule(255),
            AdaptiveCutoffRecordDescriptionLengthRule(86400),
            AdaptiveCutoffRecordLimitRangeRule(16, 3600),
            AdaptiveCutoffRecordWeightRangeRule(7.8, 4561.7),
            AdaptiveCutoffRecordCapacityRangeRule(64, 80),
            AdaptiveCutoffRecordAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
