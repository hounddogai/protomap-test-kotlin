package com.acme.transport.emissions.idling.score

import com.acme.transport.kit.text.normalize

/** A rule a manual idling score must pass. Check returns its problems, or none. */
interface ManualIdlingScoreRule {
    fun check(item: ManualIdlingScore): MutableList<String>
}

data class ManualIdlingScoreAllowedCodeRule(var allowed: MutableList<String>) : ManualIdlingScoreRule {
    override fun check(item: ManualIdlingScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingScoreRequiredNameRule(var message: String) : ManualIdlingScoreRule {
    override fun check(item: ManualIdlingScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingScoreStartsAtRangeRule(var min: Int, var max: Int) : ManualIdlingScoreRule {
    override fun check(item: ManualIdlingScore): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingScoreCodePrefixRule(var prefix: String) : ManualIdlingScoreRule {
    override fun check(item: ManualIdlingScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingScoreNameLengthRule(var maxLength: Int) : ManualIdlingScoreRule {
    override fun check(item: ManualIdlingScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual idling score. */
data class ManualIdlingScoreValidator(var rules: MutableList<ManualIdlingScoreRule>) {
    fun validate(item: ManualIdlingScore): MutableList<String> {
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

    fun requireValid(item: ManualIdlingScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIdlingScore>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIdlingScoreValidator(): ManualIdlingScoreValidator {
    return ManualIdlingScoreValidator(
        mutableListOf<ManualIdlingScoreRule>(
            ManualIdlingScoreAllowedCodeRule(mutableListOf<String>("global", "pending", "seasonal")),
            ManualIdlingScoreRequiredNameRule("manual idling score needs an entry"),
            ManualIdlingScoreStartsAtRangeRule(3600, 86400),
            ManualIdlingScoreCodePrefixRule("ACM"),
            ManualIdlingScoreNameLengthRule(64),
        ),
    )
}
