package com.acme.transport.network.zone.proposal

/** A rule a zone proposal must pass. Check returns its problems, or none. */
interface ZoneProposalRule {
    fun check(item: ZoneProposal): MutableList<String>
}

data class ZoneProposalUpdatedAtRangeRule(var min: Int, var max: Int) : ZoneProposalRule {
    override fun check(item: ZoneProposal): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class ZoneProposalRequiredTitleRule(var message: String) : ZoneProposalRule {
    override fun check(item: ZoneProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneProposalCodeLengthRule(var maxLength: Int) : ZoneProposalRule {
    override fun check(item: ZoneProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneProposalLocaleLengthRule(var maxLength: Int) : ZoneProposalRule {
    override fun check(item: ZoneProposal): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneProposalRequiredNameRule(var message: String) : ZoneProposalRule {
    override fun check(item: ZoneProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone proposal. */
data class ZoneProposalValidator(var rules: MutableList<ZoneProposalRule>) {
    fun validate(item: ZoneProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneProposalValidator(): ZoneProposalValidator {
    return ZoneProposalValidator(
        mutableListOf<ZoneProposalRule>(
            ZoneProposalUpdatedAtRangeRule(1000, 3600),
            ZoneProposalRequiredTitleRule("zone proposal needs a value"),
            ZoneProposalCodeLengthRule(80),
            ZoneProposalLocaleLengthRule(80),
            ZoneProposalRequiredNameRule("zone proposal needs an entry"),
        ),
    )
}
