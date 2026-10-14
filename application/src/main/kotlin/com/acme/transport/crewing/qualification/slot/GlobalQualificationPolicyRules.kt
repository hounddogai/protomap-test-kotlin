package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.text.normalize

/** A rule a global qualification policy must pass. Check returns its problems, or none. */
interface GlobalQualificationPolicyRule {
    fun check(item: GlobalQualificationPolicy): MutableList<String>
}

data class GlobalQualificationPolicyTrailerIdLengthRule(var maxLength: Int) : GlobalQualificationPolicyRule {
    override fun check(item: GlobalQualificationPolicy): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationPolicyAllowedTitleRule(
    var allowed: MutableList<String>,
) : GlobalQualificationPolicyRule {
    override fun check(item: GlobalQualificationPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationPolicyRequiredCodeRule(var message: String) : GlobalQualificationPolicyRule {
    override fun check(item: GlobalQualificationPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationPolicyGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : GlobalQualificationPolicyRule {
    override fun check(item: GlobalQualificationPolicy): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationPolicyRequiredTitleRule(var message: String) : GlobalQualificationPolicyRule {
    override fun check(item: GlobalQualificationPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalQualificationPolicyAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : GlobalQualificationPolicyRule {
    override fun check(item: GlobalQualificationPolicy): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global qualification policy. */
data class GlobalQualificationPolicyValidator(var rules: MutableList<GlobalQualificationPolicyRule>) {
    fun validate(item: GlobalQualificationPolicy): MutableList<String> {
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

    fun requireValid(item: GlobalQualificationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalQualificationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalQualificationPolicyValidator(): GlobalQualificationPolicyValidator {
    return GlobalQualificationPolicyValidator(
        mutableListOf<GlobalQualificationPolicyRule>(
            GlobalQualificationPolicyTrailerIdLengthRule(255),
            GlobalQualificationPolicyAllowedTitleRule(mutableListOf<String>("weighted", "scheduled", "pending")),
            GlobalQualificationPolicyRequiredCodeRule("global qualification policy needs an entry"),
            GlobalQualificationPolicyGraceDaysRangeRule(80, 1000),
            GlobalQualificationPolicyRequiredTitleRule("global qualification policy needs an entry"),
            GlobalQualificationPolicyAttemptsRangeRule(64, 64),
        ),
    )
}
