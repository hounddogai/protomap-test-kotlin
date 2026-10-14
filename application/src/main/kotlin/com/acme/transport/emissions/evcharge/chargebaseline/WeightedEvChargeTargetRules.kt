package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.text.normalize

/** A rule a weighted ev charge target must pass. Check returns its problems, or none. */
interface WeightedEvChargeTargetRule {
    fun check(item: WeightedEvChargeTarget): MutableList<String>
}

data class WeightedEvChargeTargetRequiredCodeRule(var message: String) : WeightedEvChargeTargetRule {
    override fun check(item: WeightedEvChargeTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeTargetRequiredNameRule(var message: String) : WeightedEvChargeTargetRule {
    override fun check(item: WeightedEvChargeTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeTargetRequiredTrailerIdRule(var message: String) : WeightedEvChargeTargetRule {
    override fun check(item: WeightedEvChargeTarget): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeTargetUpdatedAtRangeRule(var min: Int, var max: Int) : WeightedEvChargeTargetRule {
    override fun check(item: WeightedEvChargeTarget): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeTargetCodeLengthRule(var maxLength: Int) : WeightedEvChargeTargetRule {
    override fun check(item: WeightedEvChargeTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEvChargeTargetAllowedNameRule(
    var allowed: MutableList<String>,
) : WeightedEvChargeTargetRule {
    override fun check(item: WeightedEvChargeTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted ev charge target. */
data class WeightedEvChargeTargetValidator(var rules: MutableList<WeightedEvChargeTargetRule>) {
    fun validate(item: WeightedEvChargeTarget): MutableList<String> {
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

    fun requireValid(item: WeightedEvChargeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEvChargeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEvChargeTargetValidator(): WeightedEvChargeTargetValidator {
    return WeightedEvChargeTargetValidator(
        mutableListOf<WeightedEvChargeTargetRule>(
            WeightedEvChargeTargetRequiredCodeRule("weighted ev charge target needs an entry"),
            WeightedEvChargeTargetRequiredNameRule("weighted ev charge target needs this field"),
            WeightedEvChargeTargetRequiredTrailerIdRule("weighted ev charge target needs an entry"),
            WeightedEvChargeTargetUpdatedAtRangeRule(16, 86400),
            WeightedEvChargeTargetCodeLengthRule(3600),
            WeightedEvChargeTargetAllowedNameRule(mutableListOf<String>("tiered")),
        ),
    )
}
