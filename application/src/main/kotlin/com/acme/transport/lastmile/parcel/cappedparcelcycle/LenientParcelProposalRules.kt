package com.acme.transport.lastmile.parcel.cappedparcelcycle

/** A rule a lenient parcel proposal must pass. Check returns its problems, or none. */
interface LenientParcelProposalRule {
    fun check(item: LenientParcelProposal): MutableList<String>
}

data class LenientParcelProposalCodeLengthRule(var maxLength: Int) : LenientParcelProposalRule {
    override fun check(item: LenientParcelProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelProposalRequiredNameRule(var message: String) : LenientParcelProposalRule {
    override fun check(item: LenientParcelProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelProposalCostRangeRule(var min: Double, var max: Double) : LenientParcelProposalRule {
    override fun check(item: LenientParcelProposal): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class LenientParcelProposalRequiredCodeRule(var message: String) : LenientParcelProposalRule {
    override fun check(item: LenientParcelProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient parcel proposal. */
data class LenientParcelProposalValidator(var rules: MutableList<LenientParcelProposalRule>) {
    fun validate(item: LenientParcelProposal): MutableList<String> {
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

    fun requireValid(item: LenientParcelProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientParcelProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientParcelProposalValidator(): LenientParcelProposalValidator {
    return LenientParcelProposalValidator(
        mutableListOf<LenientParcelProposalRule>(
            LenientParcelProposalCodeLengthRule(120),
            LenientParcelProposalRequiredNameRule("lenient parcel proposal needs this field"),
            LenientParcelProposalCostRangeRule(1.6, 1003.7),
            LenientParcelProposalRequiredCodeRule("lenient parcel proposal needs this field"),
        ),
    )
}
