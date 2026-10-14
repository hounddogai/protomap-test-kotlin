package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.text.normalize

/** A rule a primary cross dock milestone must pass. Check returns its problems, or none. */
interface PrimaryCrossDockMilestoneRule {
    fun check(item: PrimaryCrossDockMilestone): MutableList<String>
}

data class PrimaryCrossDockMilestoneVersionRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryCrossDockMilestoneRule {
    override fun check(item: PrimaryCrossDockMilestone): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockMilestoneReviewerLengthRule(var maxLength: Int) : PrimaryCrossDockMilestoneRule {
    override fun check(item: PrimaryCrossDockMilestone): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockMilestoneRequiredReviewerRule(var message: String) : PrimaryCrossDockMilestoneRule {
    override fun check(item: PrimaryCrossDockMilestone): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockMilestoneAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : PrimaryCrossDockMilestoneRule {
    override fun check(item: PrimaryCrossDockMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockMilestoneExternalRefLengthRule(var maxLength: Int) : PrimaryCrossDockMilestoneRule {
    override fun check(item: PrimaryCrossDockMilestone): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCrossDockMilestoneCodePrefixRule(var prefix: String) : PrimaryCrossDockMilestoneRule {
    override fun check(item: PrimaryCrossDockMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary cross dock milestone. */
data class PrimaryCrossDockMilestoneValidator(var rules: MutableList<PrimaryCrossDockMilestoneRule>) {
    fun validate(item: PrimaryCrossDockMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryCrossDockMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCrossDockMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCrossDockMilestoneValidator(): PrimaryCrossDockMilestoneValidator {
    return PrimaryCrossDockMilestoneValidator(
        mutableListOf<PrimaryCrossDockMilestoneRule>(
            PrimaryCrossDockMilestoneVersionRangeRule(64, 80),
            PrimaryCrossDockMilestoneReviewerLengthRule(16),
            PrimaryCrossDockMilestoneRequiredReviewerRule("primary cross dock milestone needs an entry"),
            PrimaryCrossDockMilestoneAllowedExternalRefRule(
                mutableListOf<String>("default", "strict", "lenient"),
            ),
            PrimaryCrossDockMilestoneExternalRefLengthRule(255),
            PrimaryCrossDockMilestoneCodePrefixRule("OPS"),
        ),
    )
}
