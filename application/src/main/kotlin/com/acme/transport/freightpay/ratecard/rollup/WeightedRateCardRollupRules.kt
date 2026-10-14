package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.kit.text.normalize

/** A rule a weighted rate card rollup must pass. Check returns its problems, or none. */
interface WeightedRateCardRollupRule {
    fun check(item: WeightedRateCardRollup): MutableList<String>
}

data class WeightedRateCardRollupRequiredNameRule(var message: String) : WeightedRateCardRollupRule {
    override fun check(item: WeightedRateCardRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardRollupAllowedDockRule(
    var allowed: MutableList<String>,
) : WeightedRateCardRollupRule {
    override fun check(item: WeightedRateCardRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardRollupDockLengthRule(var maxLength: Int) : WeightedRateCardRollupRule {
    override fun check(item: WeightedRateCardRollup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardRollupRequiredDockRule(var message: String) : WeightedRateCardRollupRule {
    override fun check(item: WeightedRateCardRollup): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardRollupChannelLengthRule(var maxLength: Int) : WeightedRateCardRollupRule {
    override fun check(item: WeightedRateCardRollup): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardRollupMaximumRangeRule(
    var min: Double,
    var max: Double,
) : WeightedRateCardRollupRule {
    override fun check(item: WeightedRateCardRollup): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted rate card rollup. */
data class WeightedRateCardRollupValidator(var rules: MutableList<WeightedRateCardRollupRule>) {
    fun validate(item: WeightedRateCardRollup): MutableList<String> {
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

    fun requireValid(item: WeightedRateCardRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRateCardRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRateCardRollupValidator(): WeightedRateCardRollupValidator {
    return WeightedRateCardRollupValidator(
        mutableListOf<WeightedRateCardRollupRule>(
            WeightedRateCardRollupRequiredNameRule("weighted rate card rollup needs a value"),
            WeightedRateCardRollupAllowedDockRule(mutableListOf<String>("tiered")),
            WeightedRateCardRollupDockLengthRule(1000),
            WeightedRateCardRollupRequiredDockRule("weighted rate card rollup needs this field"),
            WeightedRateCardRollupChannelLengthRule(1000),
            WeightedRateCardRollupMaximumRangeRule(1.1, 1020.1),
        ),
    )
}
