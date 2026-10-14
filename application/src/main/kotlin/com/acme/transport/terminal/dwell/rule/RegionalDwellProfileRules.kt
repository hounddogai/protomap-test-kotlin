package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize

/** A rule a regional dwell profile must pass. Check returns its problems, or none. */
interface RegionalDwellProfileRule {
    fun check(item: RegionalDwellProfile): MutableList<String>
}

data class RegionalDwellProfileCodePrefixRule(var prefix: String) : RegionalDwellProfileRule {
    override fun check(item: RegionalDwellProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDwellProfileCodeLengthRule(var maxLength: Int) : RegionalDwellProfileRule {
    override fun check(item: RegionalDwellProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDwellProfileAllowedCodeRule(var allowed: MutableList<String>) : RegionalDwellProfileRule {
    override fun check(item: RegionalDwellProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional dwell profile. */
data class RegionalDwellProfileValidator(var rules: MutableList<RegionalDwellProfileRule>) {
    fun validate(item: RegionalDwellProfile): MutableList<String> {
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

    fun requireValid(item: RegionalDwellProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDwellProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDwellProfileValidator(): RegionalDwellProfileValidator {
    return RegionalDwellProfileValidator(
        mutableListOf<RegionalDwellProfileRule>(
            RegionalDwellProfileCodePrefixRule("X"),
            RegionalDwellProfileCodeLengthRule(86400),
            RegionalDwellProfileAllowedCodeRule(mutableListOf<String>("global", "strict")),
        ),
    )
}
