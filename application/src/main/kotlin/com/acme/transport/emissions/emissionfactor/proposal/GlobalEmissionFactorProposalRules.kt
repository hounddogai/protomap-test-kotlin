package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.kit.text.normalize

/** A rule a global emission factor proposal must pass. Check returns its problems, or none. */
interface GlobalEmissionFactorProposalRule {
    fun check(item: GlobalEmissionFactorProposal): MutableList<String>
}

data class GlobalEmissionFactorProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalEmissionFactorProposalRule {
    override fun check(item: GlobalEmissionFactorProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class GlobalEmissionFactorProposalRateRangeRule(
    var min: Double,
    var max: Double,
) : GlobalEmissionFactorProposalRule {
    override fun check(item: GlobalEmissionFactorProposal): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class GlobalEmissionFactorProposalRatioRangeRule(
    var min: Double,
    var max: Double,
) : GlobalEmissionFactorProposalRule {
    override fun check(item: GlobalEmissionFactorProposal): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class GlobalEmissionFactorProposalDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalEmissionFactorProposalRule {
    override fun check(item: GlobalEmissionFactorProposal): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalEmissionFactorProposalCodePrefixRule(var prefix: String) : GlobalEmissionFactorProposalRule {
    override fun check(item: GlobalEmissionFactorProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global emission factor proposal. */
data class GlobalEmissionFactorProposalValidator(var rules: MutableList<GlobalEmissionFactorProposalRule>) {
    fun validate(item: GlobalEmissionFactorProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalEmissionFactorProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalEmissionFactorProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalEmissionFactorProposalValidator(): GlobalEmissionFactorProposalValidator {
    return GlobalEmissionFactorProposalValidator(
        mutableListOf<GlobalEmissionFactorProposalRule>(
            GlobalEmissionFactorProposalAllowedCodeRule(mutableListOf<String>("legacy", "capped", "capped")),
            GlobalEmissionFactorProposalRateRangeRule(9.9, 4061.4),
            GlobalEmissionFactorProposalRatioRangeRule(4.1, 4176.2),
            GlobalEmissionFactorProposalDurationMinutesRangeRule(16, 86400),
            GlobalEmissionFactorProposalCodePrefixRule("ACM"),
        ),
    )
}
