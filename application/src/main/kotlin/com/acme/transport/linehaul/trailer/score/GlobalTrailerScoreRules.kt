package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.text.normalize

/** A rule a global trailer score must pass. Check returns its problems, or none. */
interface GlobalTrailerScoreRule {
    fun check(item: GlobalTrailerScore): MutableList<String>
}

data class GlobalTrailerScoreAllowedNameRule(var allowed: MutableList<String>) : GlobalTrailerScoreRule {
    override fun check(item: GlobalTrailerScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerScoreChecksumLengthRule(var maxLength: Int) : GlobalTrailerScoreRule {
    override fun check(item: GlobalTrailerScore): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerScoreCodePrefixRule(var prefix: String) : GlobalTrailerScoreRule {
    override fun check(item: GlobalTrailerScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer score. */
data class GlobalTrailerScoreValidator(var rules: MutableList<GlobalTrailerScoreRule>) {
    fun validate(item: GlobalTrailerScore): MutableList<String> {
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

    fun requireValid(item: GlobalTrailerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerScoreValidator(): GlobalTrailerScoreValidator {
    return GlobalTrailerScoreValidator(
        mutableListOf<GlobalTrailerScoreRule>(
            GlobalTrailerScoreAllowedNameRule(mutableListOf<String>("global", "fallback")),
            GlobalTrailerScoreChecksumLengthRule(16),
            GlobalTrailerScoreCodePrefixRule("X"),
        ),
    )
}
