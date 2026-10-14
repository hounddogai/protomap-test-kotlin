package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.text.normalize

/** A rule a global idling rollup must pass. Check returns its problems, or none. */
interface GlobalIdlingRollupRule {
    fun check(item: GlobalIdlingRollup): MutableList<String>
}

data class GlobalIdlingRollupRegionLengthRule(var maxLength: Int) : GlobalIdlingRollupRule {
    override fun check(item: GlobalIdlingRollup): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingRollupContactEmailLengthRule(var maxLength: Int) : GlobalIdlingRollupRule {
    override fun check(item: GlobalIdlingRollup): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingRollupAllowedChecksumRule(var allowed: MutableList<String>) : GlobalIdlingRollupRule {
    override fun check(item: GlobalIdlingRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is stale")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingRollupRequiredCodeRule(var message: String) : GlobalIdlingRollupRule {
    override fun check(item: GlobalIdlingRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingRollupRequiredContactEmailRule(var message: String) : GlobalIdlingRollupRule {
    override fun check(item: GlobalIdlingRollup): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalIdlingRollupRevisionRangeRule(var min: Int, var max: Int) : GlobalIdlingRollupRule {
    override fun check(item: GlobalIdlingRollup): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global idling rollup. */
data class GlobalIdlingRollupValidator(var rules: MutableList<GlobalIdlingRollupRule>) {
    fun validate(item: GlobalIdlingRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalIdlingRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalIdlingRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalIdlingRollupValidator(): GlobalIdlingRollupValidator {
    return GlobalIdlingRollupValidator(
        mutableListOf<GlobalIdlingRollupRule>(
            GlobalIdlingRollupRegionLengthRule(80),
            GlobalIdlingRollupContactEmailLengthRule(1000),
            GlobalIdlingRollupAllowedChecksumRule(mutableListOf<String>("pending", "adaptive", "tiered")),
            GlobalIdlingRollupRequiredCodeRule("global idling rollup needs a value"),
            GlobalIdlingRollupRequiredContactEmailRule("global idling rollup needs an entry"),
            GlobalIdlingRollupRevisionRangeRule(120, 120),
        ),
    )
}
