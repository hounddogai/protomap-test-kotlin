package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a strict rest break tier must pass. Check returns its problems, or none. */
interface StrictRestBreakTierRule {
    fun check(item: StrictRestBreakTier): MutableList<String>
}

data class StrictRestBreakTierRequiredCodeRule(var message: String) : StrictRestBreakTierRule {
    override fun check(item: StrictRestBreakTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakTierLoadKgRangeRule(var min: Double, var max: Double) : StrictRestBreakTierRule {
    override fun check(item: StrictRestBreakTier): MutableList<String> {
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

data class StrictRestBreakTierAllowedUnitRule(var allowed: MutableList<String>) : StrictRestBreakTierRule {
    override fun check(item: StrictRestBreakTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is missing")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakTierCodeLengthRule(var maxLength: Int) : StrictRestBreakTierRule {
    override fun check(item: StrictRestBreakTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakTierCodePrefixRule(var prefix: String) : StrictRestBreakTierRule {
    override fun check(item: StrictRestBreakTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rest break tier. */
data class StrictRestBreakTierValidator(var rules: MutableList<StrictRestBreakTierRule>) {
    fun validate(item: StrictRestBreakTier): MutableList<String> {
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

    fun requireValid(item: StrictRestBreakTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRestBreakTier>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRestBreakTierValidator(): StrictRestBreakTierValidator {
    return StrictRestBreakTierValidator(
        mutableListOf<StrictRestBreakTierRule>(
            StrictRestBreakTierRequiredCodeRule("strict rest break tier needs a value"),
            StrictRestBreakTierLoadKgRangeRule(1.9, 791.3),
            StrictRestBreakTierAllowedUnitRule(mutableListOf<String>("default")),
            StrictRestBreakTierCodeLengthRule(86400),
            StrictRestBreakTierCodePrefixRule("ACM"),
        ),
    )
}
