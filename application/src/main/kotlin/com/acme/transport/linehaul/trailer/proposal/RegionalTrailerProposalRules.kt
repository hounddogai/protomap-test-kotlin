package com.acme.transport.linehaul.trailer.proposal

/** A rule a regional trailer proposal must pass. Check returns its problems, or none. */
interface RegionalTrailerProposalRule {
    fun check(item: RegionalTrailerProposal): MutableList<String>
}

data class RegionalTrailerProposalLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RegionalTrailerProposalRule {
    override fun check(item: RegionalTrailerProposal): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerProposalCodePrefixRule(var prefix: String) : RegionalTrailerProposalRule {
    override fun check(item: RegionalTrailerProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerProposalRequiredCodeRule(var message: String) : RegionalTrailerProposalRule {
    override fun check(item: RegionalTrailerProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerProposalCodeLengthRule(var maxLength: Int) : RegionalTrailerProposalRule {
    override fun check(item: RegionalTrailerProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerProposalRequiredNameRule(var message: String) : RegionalTrailerProposalRule {
    override fun check(item: RegionalTrailerProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional trailer proposal. */
data class RegionalTrailerProposalValidator(var rules: MutableList<RegionalTrailerProposalRule>) {
    fun validate(item: RegionalTrailerProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTrailerProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerProposalValidator(): RegionalTrailerProposalValidator {
    return RegionalTrailerProposalValidator(
        mutableListOf<RegionalTrailerProposalRule>(
            RegionalTrailerProposalLoadKgRangeRule(1.8, 2480.1),
            RegionalTrailerProposalCodePrefixRule("OPS"),
            RegionalTrailerProposalRequiredCodeRule("regional trailer proposal needs a value"),
            RegionalTrailerProposalCodeLengthRule(3600),
            RegionalTrailerProposalRequiredNameRule("regional trailer proposal needs this field"),
        ),
    )
}
