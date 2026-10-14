package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.text.normalize

/** A rule a primary zone tier must pass. Check returns its problems, or none. */
interface PrimaryZoneTierRule {
    fun check(item: PrimaryZoneTier): MutableList<String>
}

data class PrimaryZoneTierWindowDaysRangeRule(var min: Int, var max: Int) : PrimaryZoneTierRule {
    override fun check(item: PrimaryZoneTier): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class PrimaryZoneTierAllowedRegionRule(var allowed: MutableList<String>) : PrimaryZoneTierRule {
    override fun check(item: PrimaryZoneTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneTierRequiredCodeRule(var message: String) : PrimaryZoneTierRule {
    override fun check(item: PrimaryZoneTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneTierTitleLengthRule(var maxLength: Int) : PrimaryZoneTierRule {
    override fun check(item: PrimaryZoneTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneTierRequiredExternalRefRule(var message: String) : PrimaryZoneTierRule {
    override fun check(item: PrimaryZoneTier): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryZoneTierCodePrefixRule(var prefix: String) : PrimaryZoneTierRule {
    override fun check(item: PrimaryZoneTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary zone tier. */
data class PrimaryZoneTierValidator(var rules: MutableList<PrimaryZoneTierRule>) {
    fun validate(item: PrimaryZoneTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryZoneTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryZoneTier>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryZoneTierValidator(): PrimaryZoneTierValidator {
    return PrimaryZoneTierValidator(
        mutableListOf<PrimaryZoneTierRule>(
            PrimaryZoneTierWindowDaysRangeRule(120, 1000),
            PrimaryZoneTierAllowedRegionRule(mutableListOf<String>("weighted")),
            PrimaryZoneTierRequiredCodeRule("primary zone tier needs this field"),
            PrimaryZoneTierTitleLengthRule(32),
            PrimaryZoneTierRequiredExternalRefRule("primary zone tier needs an entry"),
            PrimaryZoneTierCodePrefixRule("ACM"),
        ),
    )
}
