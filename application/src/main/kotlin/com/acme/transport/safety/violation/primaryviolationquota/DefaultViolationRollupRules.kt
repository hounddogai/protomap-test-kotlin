package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.text.normalize

/** A rule a default violation rollup must pass. Check returns its problems, or none. */
interface DefaultViolationRollupRule {
    fun check(item: DefaultViolationRollup): MutableList<String>
}

data class DefaultViolationRollupPinnedGuardRule(var limit: Double) : DefaultViolationRollupRule {
    override fun check(item: DefaultViolationRollup): MutableList<String> {
        if (item.pinned && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationRollupTitleLengthRule(var maxLength: Int) : DefaultViolationRollupRule {
    override fun check(item: DefaultViolationRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationRollupRequiredLabelRule(var message: String) : DefaultViolationRollupRule {
    override fun check(item: DefaultViolationRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationRollupAllowedChecksumRule(
    var allowed: MutableList<String>,
) : DefaultViolationRollupRule {
    override fun check(item: DefaultViolationRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationRollupRequiredContactEmailRule(var message: String) : DefaultViolationRollupRule {
    override fun check(item: DefaultViolationRollup): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultViolationRollupAmountRangeRule(
    var min: Double,
    var max: Double,
) : DefaultViolationRollupRule {
    override fun check(item: DefaultViolationRollup): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default violation rollup. */
data class DefaultViolationRollupValidator(var rules: MutableList<DefaultViolationRollupRule>) {
    fun validate(item: DefaultViolationRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultViolationRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultViolationRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultViolationRollupValidator(): DefaultViolationRollupValidator {
    return DefaultViolationRollupValidator(
        mutableListOf<DefaultViolationRollupRule>(
            DefaultViolationRollupPinnedGuardRule(9.6),
            DefaultViolationRollupTitleLengthRule(120),
            DefaultViolationRollupRequiredLabelRule("default violation rollup needs this field"),
            DefaultViolationRollupAllowedChecksumRule(mutableListOf<String>("adaptive")),
            DefaultViolationRollupRequiredContactEmailRule("default violation rollup needs this field"),
            DefaultViolationRollupAmountRangeRule(6.6, 1377.7),
        ),
    )
}
