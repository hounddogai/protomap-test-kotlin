package com.acme.transport.network.servicearea.batch

import com.acme.transport.kit.text.normalize

/** A rule a tiered service area batch must pass. Check returns its problems, or none. */
interface TieredServiceAreaBatchRule {
    fun check(item: TieredServiceAreaBatch): MutableList<String>
}

data class TieredServiceAreaBatchAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : TieredServiceAreaBatchRule {
    override fun check(item: TieredServiceAreaBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaBatchCodePrefixRule(var prefix: String) : TieredServiceAreaBatchRule {
    override fun check(item: TieredServiceAreaBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaBatchTitleLengthRule(var maxLength: Int) : TieredServiceAreaBatchRule {
    override fun check(item: TieredServiceAreaBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaBatchFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : TieredServiceAreaBatchRule {
    override fun check(item: TieredServiceAreaBatch): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class TieredServiceAreaBatchCodeLengthRule(var maxLength: Int) : TieredServiceAreaBatchRule {
    override fun check(item: TieredServiceAreaBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServiceAreaBatchRequiredCountryRule(var message: String) : TieredServiceAreaBatchRule {
    override fun check(item: TieredServiceAreaBatch): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered service area batch. */
data class TieredServiceAreaBatchValidator(var rules: MutableList<TieredServiceAreaBatchRule>) {
    fun validate(item: TieredServiceAreaBatch): MutableList<String> {
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

    fun requireValid(item: TieredServiceAreaBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredServiceAreaBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredServiceAreaBatchValidator(): TieredServiceAreaBatchValidator {
    return TieredServiceAreaBatchValidator(
        mutableListOf<TieredServiceAreaBatchRule>(
            TieredServiceAreaBatchAllowedShiftCodeRule(
                mutableListOf<String>("scheduled", "fallback", "regional"),
            ),
            TieredServiceAreaBatchCodePrefixRule("Q"),
            TieredServiceAreaBatchTitleLengthRule(32),
            TieredServiceAreaBatchFloorValueRangeRule(7.7, 1082.4),
            TieredServiceAreaBatchCodeLengthRule(32),
            TieredServiceAreaBatchRequiredCountryRule("tiered service area batch needs this field"),
        ),
    )
}
