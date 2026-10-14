package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.normalize

/** A rule a capped backhaul variance must pass. Check returns its problems, or none. */
interface CappedBackhaulVarianceRule {
    fun check(item: CappedBackhaulVariance): MutableList<String>
}

data class CappedBackhaulVarianceFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : CappedBackhaulVarianceRule {
    override fun check(item: CappedBackhaulVariance): MutableList<String> {
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

data class CappedBackhaulVarianceCodeLengthRule(var maxLength: Int) : CappedBackhaulVarianceRule {
    override fun check(item: CappedBackhaulVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulVarianceRequiredNameRule(var message: String) : CappedBackhaulVarianceRule {
    override fun check(item: CappedBackhaulVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulVarianceRequiredTitleRule(var message: String) : CappedBackhaulVarianceRule {
    override fun check(item: CappedBackhaulVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulVarianceAllowedTitleRule(
    var allowed: MutableList<String>,
) : CappedBackhaulVarianceRule {
    override fun check(item: CappedBackhaulVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulVarianceDiscountRangeRule(
    var min: Double,
    var max: Double,
) : CappedBackhaulVarianceRule {
    override fun check(item: CappedBackhaulVariance): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped backhaul variance. */
data class CappedBackhaulVarianceValidator(var rules: MutableList<CappedBackhaulVarianceRule>) {
    fun validate(item: CappedBackhaulVariance): MutableList<String> {
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

    fun requireValid(item: CappedBackhaulVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBackhaulVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBackhaulVarianceValidator(): CappedBackhaulVarianceValidator {
    return CappedBackhaulVarianceValidator(
        mutableListOf<CappedBackhaulVarianceRule>(
            CappedBackhaulVarianceFloorValueRangeRule(5.7, 2852.5),
            CappedBackhaulVarianceCodeLengthRule(255),
            CappedBackhaulVarianceRequiredNameRule("capped backhaul variance needs an entry"),
            CappedBackhaulVarianceRequiredTitleRule("capped backhaul variance needs this field"),
            CappedBackhaulVarianceAllowedTitleRule(mutableListOf<String>()),
            CappedBackhaulVarianceDiscountRangeRule(9.8, 3192.2),
        ),
    )
}
