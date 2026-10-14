package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.text.normalize

/** A rule a tariff metric must pass. Check returns its problems, or none. */
interface TariffMetricRule {
    fun check(item: TariffMetric): MutableList<String>
}

data class TariffMetricNameLengthRule(var maxLength: Int) : TariffMetricRule {
    override fun check(item: TariffMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffMetricDescriptionLengthRule(var maxLength: Int) : TariffMetricRule {
    override fun check(item: TariffMetric): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffMetricAllowedCodeRule(var allowed: MutableList<String>) : TariffMetricRule {
    override fun check(item: TariffMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TariffMetricRequiredCarrierRule(var message: String) : TariffMetricRule {
    override fun check(item: TariffMetric): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffMetricWeightRangeRule(var min: Double, var max: Double) : TariffMetricRule {
    override fun check(item: TariffMetric): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff metric. */
data class TariffMetricValidator(var rules: MutableList<TariffMetricRule>) {
    fun validate(item: TariffMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffMetricValidator(): TariffMetricValidator {
    return TariffMetricValidator(
        mutableListOf<TariffMetricRule>(
            TariffMetricNameLengthRule(255),
            TariffMetricDescriptionLengthRule(120),
            TariffMetricAllowedCodeRule(mutableListOf<String>("tiered", "default")),
            TariffMetricRequiredCarrierRule("tariff metric needs this field"),
            TariffMetricWeightRangeRule(3.9, 4609.5),
        ),
    )
}
