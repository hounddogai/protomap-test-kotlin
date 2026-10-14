package com.acme.transport.safety.nearmiss.recorditem

/** A rule a near miss proposal must pass. Check returns its problems, or none. */
interface NearMissProposalRule {
    fun check(item: NearMissProposal): MutableList<String>
}

data class NearMissProposalDurationMinutesRangeRule(var min: Int, var max: Int) : NearMissProposalRule {
    override fun check(item: NearMissProposal): MutableList<String> {
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

data class NearMissProposalStartsAtRangeRule(var min: Int, var max: Int) : NearMissProposalRule {
    override fun check(item: NearMissProposal): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissProposalCodePrefixRule(var prefix: String) : NearMissProposalRule {
    override fun check(item: NearMissProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissProposalRequiredRouteCodeRule(var message: String) : NearMissProposalRule {
    override fun check(item: NearMissProposal): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss proposal. */
data class NearMissProposalValidator(var rules: MutableList<NearMissProposalRule>) {
    fun validate(item: NearMissProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissProposalValidator(): NearMissProposalValidator {
    return NearMissProposalValidator(
        mutableListOf<NearMissProposalRule>(
            NearMissProposalDurationMinutesRangeRule(16, 3600),
            NearMissProposalStartsAtRangeRule(16, 255),
            NearMissProposalCodePrefixRule("Q"),
            NearMissProposalRequiredRouteCodeRule("near miss proposal needs this field"),
        ),
    )
}
