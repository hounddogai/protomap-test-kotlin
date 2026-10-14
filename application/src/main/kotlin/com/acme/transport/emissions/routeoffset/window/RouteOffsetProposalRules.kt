package com.acme.transport.emissions.routeoffset.window

/** A rule a route offset proposal must pass. Check returns its problems, or none. */
interface RouteOffsetProposalRule {
    fun check(item: RouteOffsetProposal): MutableList<String>
}

data class RouteOffsetProposalFuelLitersRangeRule(var min: Double, var max: Double) : RouteOffsetProposalRule {
    override fun check(item: RouteOffsetProposal): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetProposalTitleLengthRule(var maxLength: Int) : RouteOffsetProposalRule {
    override fun check(item: RouteOffsetProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetProposalRequiredNotesRule(var message: String) : RouteOffsetProposalRule {
    override fun check(item: RouteOffsetProposal): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetProposalCodePrefixRule(var prefix: String) : RouteOffsetProposalRule {
    override fun check(item: RouteOffsetProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset proposal. */
data class RouteOffsetProposalValidator(var rules: MutableList<RouteOffsetProposalRule>) {
    fun validate(item: RouteOffsetProposal): MutableList<String> {
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

    fun requireValid(item: RouteOffsetProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetProposalValidator(): RouteOffsetProposalValidator {
    return RouteOffsetProposalValidator(
        mutableListOf<RouteOffsetProposalRule>(
            RouteOffsetProposalFuelLitersRangeRule(3.8, 191.8),
            RouteOffsetProposalTitleLengthRule(255),
            RouteOffsetProposalRequiredNotesRule("route offset proposal needs a value"),
            RouteOffsetProposalCodePrefixRule("ACM"),
        ),
    )
}
