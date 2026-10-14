package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a pending cutoff override must pass. Check returns its problems, or none. */
interface PendingCutoffOverrideRule {
    fun check(item: PendingCutoffOverride): MutableList<String>
}

data class PendingCutoffOverrideRequiredCodeRule(var message: String) : PendingCutoffOverrideRule {
    override fun check(item: PendingCutoffOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffOverrideRequiredSegmentRule(var message: String) : PendingCutoffOverrideRule {
    override fun check(item: PendingCutoffOverride): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffOverrideAllowedTitleRule(var allowed: MutableList<String>) : PendingCutoffOverrideRule {
    override fun check(item: PendingCutoffOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffOverrideTitleLengthRule(var maxLength: Int) : PendingCutoffOverrideRule {
    override fun check(item: PendingCutoffOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffOverrideAmountRangeRule(var min: Double, var max: Double) : PendingCutoffOverrideRule {
    override fun check(item: PendingCutoffOverride): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending cutoff override. */
data class PendingCutoffOverrideValidator(var rules: MutableList<PendingCutoffOverrideRule>) {
    fun validate(item: PendingCutoffOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCutoffOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCutoffOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCutoffOverrideValidator(): PendingCutoffOverrideValidator {
    return PendingCutoffOverrideValidator(
        mutableListOf<PendingCutoffOverrideRule>(
            PendingCutoffOverrideRequiredCodeRule("pending cutoff override needs this field"),
            PendingCutoffOverrideRequiredSegmentRule("pending cutoff override needs this field"),
            PendingCutoffOverrideAllowedTitleRule(mutableListOf<String>("primary")),
            PendingCutoffOverrideTitleLengthRule(120),
            PendingCutoffOverrideAmountRangeRule(7.8, 3366.0),
        ),
    )
}
