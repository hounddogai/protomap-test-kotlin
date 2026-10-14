package com.acme.transport.customs.clearance.rollup

import com.acme.transport.kit.text.normalize

/** A rule a regional clearance rollup must pass. Check returns its problems, or none. */
interface RegionalClearanceRollupRule {
    fun check(item: RegionalClearanceRollup): MutableList<String>
}

data class RegionalClearanceRollupRequiredTitleRule(var message: String) : RegionalClearanceRollupRule {
    override fun check(item: RegionalClearanceRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceRollupActiveGuardRule(var limit: Double) : RegionalClearanceRollupRule {
    override fun check(item: RegionalClearanceRollup): MutableList<String> {
        if (item.active && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceRollupCodeLengthRule(var maxLength: Int) : RegionalClearanceRollupRule {
    override fun check(item: RegionalClearanceRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceRollupTitleLengthRule(var maxLength: Int) : RegionalClearanceRollupRule {
    override fun check(item: RegionalClearanceRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceRollupNameLengthRule(var maxLength: Int) : RegionalClearanceRollupRule {
    override fun check(item: RegionalClearanceRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalClearanceRollupAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalClearanceRollupRule {
    override fun check(item: RegionalClearanceRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional clearance rollup. */
data class RegionalClearanceRollupValidator(var rules: MutableList<RegionalClearanceRollupRule>) {
    fun validate(item: RegionalClearanceRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalClearanceRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalClearanceRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalClearanceRollupValidator(): RegionalClearanceRollupValidator {
    return RegionalClearanceRollupValidator(
        mutableListOf<RegionalClearanceRollupRule>(
            RegionalClearanceRollupRequiredTitleRule("regional clearance rollup needs this field"),
            RegionalClearanceRollupActiveGuardRule(9.6),
            RegionalClearanceRollupCodeLengthRule(120),
            RegionalClearanceRollupTitleLengthRule(32),
            RegionalClearanceRollupNameLengthRule(120),
            RegionalClearanceRollupAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
