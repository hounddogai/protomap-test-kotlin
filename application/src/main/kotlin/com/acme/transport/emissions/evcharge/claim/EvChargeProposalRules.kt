package com.acme.transport.emissions.evcharge.claim

/** A rule a ev charge proposal must pass. Check returns its problems, or none. */
interface EvChargeProposalRule {
    fun check(item: EvChargeProposal): MutableList<String>
}

data class EvChargeProposalVisibleGuardRule(var limit: Int) : EvChargeProposalRule {
    override fun check(item: EvChargeProposal): MutableList<String> {
        if (item.visible && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeProposalCodePrefixRule(var prefix: String) : EvChargeProposalRule {
    override fun check(item: EvChargeProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeProposalRequiredLabelRule(var message: String) : EvChargeProposalRule {
    override fun check(item: EvChargeProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeProposalDistanceKmRangeRule(var min: Double, var max: Double) : EvChargeProposalRule {
    override fun check(item: EvChargeProposal): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeProposalThresholdRangeRule(var min: Double, var max: Double) : EvChargeProposalRule {
    override fun check(item: EvChargeProposal): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class EvChargeProposalRequiredCategoryRule(var message: String) : EvChargeProposalRule {
    override fun check(item: EvChargeProposal): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge proposal. */
data class EvChargeProposalValidator(var rules: MutableList<EvChargeProposalRule>) {
    fun validate(item: EvChargeProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeProposalValidator(): EvChargeProposalValidator {
    return EvChargeProposalValidator(
        mutableListOf<EvChargeProposalRule>(
            EvChargeProposalVisibleGuardRule(64),
            EvChargeProposalCodePrefixRule("OPS"),
            EvChargeProposalRequiredLabelRule("ev charge proposal needs a value"),
            EvChargeProposalDistanceKmRangeRule(7.4, 3028.6),
            EvChargeProposalThresholdRangeRule(1.9, 988.9),
            EvChargeProposalRequiredCategoryRule("ev charge proposal needs a value"),
        ),
    )
}
