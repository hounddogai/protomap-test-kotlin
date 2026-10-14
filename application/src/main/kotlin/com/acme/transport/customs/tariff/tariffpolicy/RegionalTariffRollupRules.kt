package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.normalize

/** A rule a regional tariff rollup must pass. Check returns its problems, or none. */
interface RegionalTariffRollupRule {
    fun check(item: RegionalTariffRollup): MutableList<String>
}

data class RegionalTariffRollupExternalRefLengthRule(var maxLength: Int) : RegionalTariffRollupRule {
    override fun check(item: RegionalTariffRollup): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffRollupRequiredNameRule(var message: String) : RegionalTariffRollupRule {
    override fun check(item: RegionalTariffRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffRollupAllowedCategoryRule(
    var allowed: MutableList<String>,
) : RegionalTariffRollupRule {
    override fun check(item: RegionalTariffRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffRollupCategoryLengthRule(var maxLength: Int) : RegionalTariffRollupRule {
    override fun check(item: RegionalTariffRollup): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTariffRollupMinimumRangeRule(var min: Double, var max: Double) : RegionalTariffRollupRule {
    override fun check(item: RegionalTariffRollup): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class RegionalTariffRollupRequiredCodeRule(var message: String) : RegionalTariffRollupRule {
    override fun check(item: RegionalTariffRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional tariff rollup. */
data class RegionalTariffRollupValidator(var rules: MutableList<RegionalTariffRollupRule>) {
    fun validate(item: RegionalTariffRollup): MutableList<String> {
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

    fun requireValid(item: RegionalTariffRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTariffRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTariffRollupValidator(): RegionalTariffRollupValidator {
    return RegionalTariffRollupValidator(
        mutableListOf<RegionalTariffRollupRule>(
            RegionalTariffRollupExternalRefLengthRule(3600),
            RegionalTariffRollupRequiredNameRule("regional tariff rollup needs a value"),
            RegionalTariffRollupAllowedCategoryRule(mutableListOf<String>("legacy", "capped")),
            RegionalTariffRollupCategoryLengthRule(1000),
            RegionalTariffRollupMinimumRangeRule(7.4, 3829.2),
            RegionalTariffRollupRequiredCodeRule("regional tariff rollup needs an entry"),
        ),
    )
}
