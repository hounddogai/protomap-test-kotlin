package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.normalize

/** A rule a global parcel batch must pass. Check returns its problems, or none. */
interface GlobalParcelBatchRule {
    fun check(item: GlobalParcelBatch): MutableList<String>
}

data class GlobalParcelBatchLoadKgRangeRule(var min: Double, var max: Double) : GlobalParcelBatchRule {
    override fun check(item: GlobalParcelBatch): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class GlobalParcelBatchAllowedCountryRule(var allowed: MutableList<String>) : GlobalParcelBatchRule {
    override fun check(item: GlobalParcelBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelBatchCodePrefixRule(var prefix: String) : GlobalParcelBatchRule {
    override fun check(item: GlobalParcelBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelBatchCodeLengthRule(var maxLength: Int) : GlobalParcelBatchRule {
    override fun check(item: GlobalParcelBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalParcelBatchFuelLitersRangeRule(var min: Double, var max: Double) : GlobalParcelBatchRule {
    override fun check(item: GlobalParcelBatch): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class GlobalParcelBatchRequiredNameRule(var message: String) : GlobalParcelBatchRule {
    override fun check(item: GlobalParcelBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global parcel batch. */
data class GlobalParcelBatchValidator(var rules: MutableList<GlobalParcelBatchRule>) {
    fun validate(item: GlobalParcelBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalParcelBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalParcelBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalParcelBatchValidator(): GlobalParcelBatchValidator {
    return GlobalParcelBatchValidator(
        mutableListOf<GlobalParcelBatchRule>(
            GlobalParcelBatchLoadKgRangeRule(9.4, 3533.0),
            GlobalParcelBatchAllowedCountryRule(mutableListOf<String>("pending", "weighted")),
            GlobalParcelBatchCodePrefixRule("X"),
            GlobalParcelBatchCodeLengthRule(3600),
            GlobalParcelBatchFuelLitersRangeRule(4.2, 1113.6),
            GlobalParcelBatchRequiredNameRule("global parcel batch needs an entry"),
        ),
    )
}
