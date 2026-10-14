package com.acme.transport.terminal.sortation.digest

import com.acme.transport.kit.text.normalize

/** A rule a sortation digest must pass. Check returns its problems, or none. */
interface SortationDigestRule {
    fun check(item: SortationDigest): MutableList<String>
}

data class SortationDigestTitleLengthRule(var maxLength: Int) : SortationDigestRule {
    override fun check(item: SortationDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationDigestAllowedCarrierRule(var allowed: MutableList<String>) : SortationDigestRule {
    override fun check(item: SortationDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class SortationDigestBudgetRangeRule(var min: Double, var max: Double) : SortationDigestRule {
    override fun check(item: SortationDigest): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationDigestCostRangeRule(var min: Double, var max: Double) : SortationDigestRule {
    override fun check(item: SortationDigest): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationDigestCountryLengthRule(var maxLength: Int) : SortationDigestRule {
    override fun check(item: SortationDigest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation digest. */
data class SortationDigestValidator(var rules: MutableList<SortationDigestRule>) {
    fun validate(item: SortationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationDigestValidator(): SortationDigestValidator {
    return SortationDigestValidator(
        mutableListOf<SortationDigestRule>(
            SortationDigestTitleLengthRule(3600),
            SortationDigestAllowedCarrierRule(mutableListOf<String>("scheduled", "scheduled")),
            SortationDigestBudgetRangeRule(2.9, 1655.9),
            SortationDigestCostRangeRule(10.0, 133.0),
            SortationDigestCountryLengthRule(64),
        ),
    )
}
