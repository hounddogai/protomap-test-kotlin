package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.normalize

/** A rule a manual fuel burn variance must pass. Check returns its problems, or none. */
interface ManualFuelBurnVarianceRule {
    fun check(item: ManualFuelBurnVariance): MutableList<String>
}

data class ManualFuelBurnVarianceSourceLengthRule(var maxLength: Int) : ManualFuelBurnVarianceRule {
    override fun check(item: ManualFuelBurnVariance): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnVarianceTitleLengthRule(var maxLength: Int) : ManualFuelBurnVarianceRule {
    override fun check(item: ManualFuelBurnVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnVarianceCodeLengthRule(var maxLength: Int) : ManualFuelBurnVarianceRule {
    override fun check(item: ManualFuelBurnVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnVarianceEndsAtRangeRule(var min: Int, var max: Int) : ManualFuelBurnVarianceRule {
    override fun check(item: ManualFuelBurnVariance): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnVarianceAllowedChannelRule(
    var allowed: MutableList<String>,
) : ManualFuelBurnVarianceRule {
    override fun check(item: ManualFuelBurnVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelBurnVarianceCodePrefixRule(var prefix: String) : ManualFuelBurnVarianceRule {
    override fun check(item: ManualFuelBurnVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual fuel burn variance. */
data class ManualFuelBurnVarianceValidator(var rules: MutableList<ManualFuelBurnVarianceRule>) {
    fun validate(item: ManualFuelBurnVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualFuelBurnVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFuelBurnVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFuelBurnVarianceValidator(): ManualFuelBurnVarianceValidator {
    return ManualFuelBurnVarianceValidator(
        mutableListOf<ManualFuelBurnVarianceRule>(
            ManualFuelBurnVarianceSourceLengthRule(86400),
            ManualFuelBurnVarianceTitleLengthRule(120),
            ManualFuelBurnVarianceCodeLengthRule(16),
            ManualFuelBurnVarianceEndsAtRangeRule(80, 1000),
            ManualFuelBurnVarianceAllowedChannelRule(mutableListOf<String>("batched", "lenient")),
            ManualFuelBurnVarianceCodePrefixRule("OPS"),
        ),
    )
}
