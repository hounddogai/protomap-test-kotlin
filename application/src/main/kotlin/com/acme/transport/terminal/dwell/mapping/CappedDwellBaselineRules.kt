package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.text.normalize

/** A rule a capped dwell baseline must pass. Check returns its problems, or none. */
interface CappedDwellBaselineRule {
    fun check(item: CappedDwellBaseline): MutableList<String>
}

data class CappedDwellBaselineAllowedCodeRule(var allowed: MutableList<String>) : CappedDwellBaselineRule {
    override fun check(item: CappedDwellBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellBaselineFuelLitersRangeRule(var min: Double, var max: Double) : CappedDwellBaselineRule {
    override fun check(item: CappedDwellBaseline): MutableList<String> {
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

data class CappedDwellBaselineCodePrefixRule(var prefix: String) : CappedDwellBaselineRule {
    override fun check(item: CappedDwellBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellBaselineLoadKgRangeRule(var min: Double, var max: Double) : CappedDwellBaselineRule {
    override fun check(item: CappedDwellBaseline): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dwell baseline. */
data class CappedDwellBaselineValidator(var rules: MutableList<CappedDwellBaselineRule>) {
    fun validate(item: CappedDwellBaseline): MutableList<String> {
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

    fun requireValid(item: CappedDwellBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDwellBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDwellBaselineValidator(): CappedDwellBaselineValidator {
    return CappedDwellBaselineValidator(
        mutableListOf<CappedDwellBaselineRule>(
            CappedDwellBaselineAllowedCodeRule(mutableListOf<String>("rolling", "lenient")),
            CappedDwellBaselineFuelLitersRangeRule(6.8, 3882.3),
            CappedDwellBaselineCodePrefixRule("ACM"),
            CappedDwellBaselineLoadKgRangeRule(9.1, 3967.7),
        ),
    )
}
