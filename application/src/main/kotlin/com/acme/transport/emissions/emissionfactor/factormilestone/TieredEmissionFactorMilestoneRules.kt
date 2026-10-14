package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.text.normalize

/** A rule a tiered emission factor milestone must pass. Check returns its problems, or none. */
interface TieredEmissionFactorMilestoneRule {
    fun check(item: TieredEmissionFactorMilestone): MutableList<String>
}

data class TieredEmissionFactorMilestoneAllowedSourceRule(
    var allowed: MutableList<String>,
) : TieredEmissionFactorMilestoneRule {
    override fun check(item: TieredEmissionFactorMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorMilestoneCodePrefixRule(var prefix: String) : TieredEmissionFactorMilestoneRule {
    override fun check(item: TieredEmissionFactorMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorMilestoneRequiredLabelRule(
    var message: String,
) : TieredEmissionFactorMilestoneRule {
    override fun check(item: TieredEmissionFactorMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorMilestoneRequiredSourceRule(
    var message: String,
) : TieredEmissionFactorMilestoneRule {
    override fun check(item: TieredEmissionFactorMilestone): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorMilestoneDescriptionLengthRule(
    var maxLength: Int,
) : TieredEmissionFactorMilestoneRule {
    override fun check(item: TieredEmissionFactorMilestone): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorMilestoneLabelLengthRule(
    var maxLength: Int,
) : TieredEmissionFactorMilestoneRule {
    override fun check(item: TieredEmissionFactorMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered emission factor milestone. */
data class TieredEmissionFactorMilestoneValidator(var rules: MutableList<TieredEmissionFactorMilestoneRule>) {
    fun validate(item: TieredEmissionFactorMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredEmissionFactorMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEmissionFactorMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEmissionFactorMilestoneValidator(): TieredEmissionFactorMilestoneValidator {
    return TieredEmissionFactorMilestoneValidator(
        mutableListOf<TieredEmissionFactorMilestoneRule>(
            TieredEmissionFactorMilestoneAllowedSourceRule(
                mutableListOf<String>("seasonal", "pending", "seasonal"),
            ),
            TieredEmissionFactorMilestoneCodePrefixRule("Q"),
            TieredEmissionFactorMilestoneRequiredLabelRule("tiered emission factor milestone needs this field"),
            TieredEmissionFactorMilestoneRequiredSourceRule("tiered emission factor milestone needs an entry"),
            TieredEmissionFactorMilestoneDescriptionLengthRule(86400),
            TieredEmissionFactorMilestoneLabelLengthRule(16),
        ),
    )
}
