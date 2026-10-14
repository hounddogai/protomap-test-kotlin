package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.text.normalize

/** A rule a global locker score must pass. Check returns its problems, or none. */
interface GlobalLockerScoreRule {
    fun check(item: GlobalLockerScore): MutableList<String>
}

data class GlobalLockerScoreLoadKgRangeRule(var min: Double, var max: Double) : GlobalLockerScoreRule {
    override fun check(item: GlobalLockerScore): MutableList<String> {
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

data class GlobalLockerScoreCodePrefixRule(var prefix: String) : GlobalLockerScoreRule {
    override fun check(item: GlobalLockerScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerScoreRequiredSegmentRule(var message: String) : GlobalLockerScoreRule {
    override fun check(item: GlobalLockerScore): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerScoreAllowedNameRule(var allowed: MutableList<String>) : GlobalLockerScoreRule {
    override fun check(item: GlobalLockerScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerScoreCategoryLengthRule(var maxLength: Int) : GlobalLockerScoreRule {
    override fun check(item: GlobalLockerScore): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerScoreRequiredCategoryRule(var message: String) : GlobalLockerScoreRule {
    override fun check(item: GlobalLockerScore): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global locker score. */
data class GlobalLockerScoreValidator(var rules: MutableList<GlobalLockerScoreRule>) {
    fun validate(item: GlobalLockerScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalLockerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLockerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLockerScoreValidator(): GlobalLockerScoreValidator {
    return GlobalLockerScoreValidator(
        mutableListOf<GlobalLockerScoreRule>(
            GlobalLockerScoreLoadKgRangeRule(3.4, 2129.1),
            GlobalLockerScoreCodePrefixRule("REF"),
            GlobalLockerScoreRequiredSegmentRule("global locker score needs this field"),
            GlobalLockerScoreAllowedNameRule(mutableListOf<String>("default", "rolling")),
            GlobalLockerScoreCategoryLengthRule(16),
            GlobalLockerScoreRequiredCategoryRule("global locker score needs a value"),
        ),
    )
}
