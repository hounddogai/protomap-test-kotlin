package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.text.normalize

/** A rule a manual attempt request must pass. Check returns its problems, or none. */
interface ManualAttemptRequestRule {
    fun check(item: ManualAttemptRequest): MutableList<String>
}

data class ManualAttemptRequestRequiredTitleRule(var message: String) : ManualAttemptRequestRule {
    override fun check(item: ManualAttemptRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptRequestScoreRangeRule(var min: Double, var max: Double) : ManualAttemptRequestRule {
    override fun check(item: ManualAttemptRequest): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ManualAttemptRequestNameLengthRule(var maxLength: Int) : ManualAttemptRequestRule {
    override fun check(item: ManualAttemptRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptRequestCodePrefixRule(var prefix: String) : ManualAttemptRequestRule {
    override fun check(item: ManualAttemptRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptRequestAllowedCodeRule(var allowed: MutableList<String>) : ManualAttemptRequestRule {
    override fun check(item: ManualAttemptRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual attempt request. */
data class ManualAttemptRequestValidator(var rules: MutableList<ManualAttemptRequestRule>) {
    fun validate(item: ManualAttemptRequest): MutableList<String> {
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

    fun requireValid(item: ManualAttemptRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAttemptRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAttemptRequestValidator(): ManualAttemptRequestValidator {
    return ManualAttemptRequestValidator(
        mutableListOf<ManualAttemptRequestRule>(
            ManualAttemptRequestRequiredTitleRule("manual attempt request needs this field"),
            ManualAttemptRequestScoreRangeRule(5.4, 1665.7),
            ManualAttemptRequestNameLengthRule(255),
            ManualAttemptRequestCodePrefixRule("Q"),
            ManualAttemptRequestAllowedCodeRule(mutableListOf<String>("weighted", "strict")),
        ),
    )
}
