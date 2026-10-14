package com.acme.transport.network.node.batch

/** A rule a rolling node proposal must pass. Check returns its problems, or none. */
interface RollingNodeProposalRule {
    fun check(item: RollingNodeProposal): MutableList<String>
}

data class RollingNodeProposalRatioRangeRule(var min: Double, var max: Double) : RollingNodeProposalRule {
    override fun check(item: RollingNodeProposal): MutableList<String> {
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

data class RollingNodeProposalRouteCodeLengthRule(var maxLength: Int) : RollingNodeProposalRule {
    override fun check(item: RollingNodeProposal): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingNodeProposalRequiredRouteCodeRule(var message: String) : RollingNodeProposalRule {
    override fun check(item: RollingNodeProposal): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling node proposal. */
data class RollingNodeProposalValidator(var rules: MutableList<RollingNodeProposalRule>) {
    fun validate(item: RollingNodeProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingNodeProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingNodeProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingNodeProposalValidator(): RollingNodeProposalValidator {
    return RollingNodeProposalValidator(
        mutableListOf<RollingNodeProposalRule>(
            RollingNodeProposalRatioRangeRule(2.0, 4174.0),
            RollingNodeProposalRouteCodeLengthRule(255),
            RollingNodeProposalRequiredRouteCodeRule("rolling node proposal needs a value"),
        ),
    )
}
