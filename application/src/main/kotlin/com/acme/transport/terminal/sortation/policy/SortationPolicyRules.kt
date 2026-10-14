package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.text.normalize

/** A rule a sortation policy must pass. Check returns its problems, or none. */
interface SortationPolicyRule {
    fun check(item: SortationPolicy): MutableList<String>
}

data class SortationPolicyRequiredChecksumRule(var message: String) : SortationPolicyRule {
    override fun check(item: SortationPolicy): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationPolicyNameLengthRule(var maxLength: Int) : SortationPolicyRule {
    override fun check(item: SortationPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationPolicyScoreRangeRule(var min: Double, var max: Double) : SortationPolicyRule {
    override fun check(item: SortationPolicy): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationPolicyChecksumLengthRule(var maxLength: Int) : SortationPolicyRule {
    override fun check(item: SortationPolicy): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationPolicyAllowedNameRule(var allowed: MutableList<String>) : SortationPolicyRule {
    override fun check(item: SortationPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation policy. */
data class SortationPolicyValidator(var rules: MutableList<SortationPolicyRule>) {
    fun validate(item: SortationPolicy): MutableList<String> {
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

    fun requireValid(item: SortationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationPolicyValidator(): SortationPolicyValidator {
    return SortationPolicyValidator(
        mutableListOf<SortationPolicyRule>(
            SortationPolicyRequiredChecksumRule("sortation policy needs this field"),
            SortationPolicyNameLengthRule(255),
            SortationPolicyScoreRangeRule(8.5, 4134.5),
            SortationPolicyChecksumLengthRule(86400),
            SortationPolicyAllowedNameRule(mutableListOf<String>("batched")),
        ),
    )
}
