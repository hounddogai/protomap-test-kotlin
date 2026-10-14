package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.text.normalize

/** A rule a legacy trailer yard bundle must pass. Check returns its problems, or none. */
interface LegacyTrailerYardBundleRule {
    fun check(item: LegacyTrailerYardBundle): MutableList<String>
}

data class LegacyTrailerYardBundleMarginRangeRule(
    var min: Double,
    var max: Double,
) : LegacyTrailerYardBundleRule {
    override fun check(item: LegacyTrailerYardBundle): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class LegacyTrailerYardBundleRequiredDockRule(var message: String) : LegacyTrailerYardBundleRule {
    override fun check(item: LegacyTrailerYardBundle): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardBundleAllowedNameRule(
    var allowed: MutableList<String>,
) : LegacyTrailerYardBundleRule {
    override fun check(item: LegacyTrailerYardBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardBundleDockLengthRule(var maxLength: Int) : LegacyTrailerYardBundleRule {
    override fun check(item: LegacyTrailerYardBundle): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy trailer yard bundle. */
data class LegacyTrailerYardBundleValidator(var rules: MutableList<LegacyTrailerYardBundleRule>) {
    fun validate(item: LegacyTrailerYardBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTrailerYardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTrailerYardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTrailerYardBundleValidator(): LegacyTrailerYardBundleValidator {
    return LegacyTrailerYardBundleValidator(
        mutableListOf<LegacyTrailerYardBundleRule>(
            LegacyTrailerYardBundleMarginRangeRule(7.2, 3033.2),
            LegacyTrailerYardBundleRequiredDockRule("legacy trailer yard bundle needs an entry"),
            LegacyTrailerYardBundleAllowedNameRule(mutableListOf<String>("strict")),
            LegacyTrailerYardBundleDockLengthRule(80),
        ),
    )
}
