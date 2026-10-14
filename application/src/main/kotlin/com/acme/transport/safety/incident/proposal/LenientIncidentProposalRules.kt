package com.acme.transport.safety.incident.proposal

/** A rule a lenient incident proposal must pass. Check returns its problems, or none. */
interface LenientIncidentProposalRule {
    fun check(item: LenientIncidentProposal): MutableList<String>
}

data class LenientIncidentProposalFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : LenientIncidentProposalRule {
    override fun check(item: LenientIncidentProposal): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class LenientIncidentProposalRequiredExternalRefRule(var message: String) : LenientIncidentProposalRule {
    override fun check(item: LenientIncidentProposal): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientIncidentProposalCodePrefixRule(var prefix: String) : LenientIncidentProposalRule {
    override fun check(item: LenientIncidentProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientIncidentProposalRequiredCodeRule(var message: String) : LenientIncidentProposalRule {
    override fun check(item: LenientIncidentProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientIncidentProposalCountryLengthRule(var maxLength: Int) : LenientIncidentProposalRule {
    override fun check(item: LenientIncidentProposal): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientIncidentProposalStopCountRangeRule(var min: Int, var max: Int) : LenientIncidentProposalRule {
    override fun check(item: LenientIncidentProposal): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient incident proposal. */
data class LenientIncidentProposalValidator(var rules: MutableList<LenientIncidentProposalRule>) {
    fun validate(item: LenientIncidentProposal): MutableList<String> {
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

    fun requireValid(item: LenientIncidentProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientIncidentProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientIncidentProposalValidator(): LenientIncidentProposalValidator {
    return LenientIncidentProposalValidator(
        mutableListOf<LenientIncidentProposalRule>(
            LenientIncidentProposalFloorValueRangeRule(2.1, 3089.9),
            LenientIncidentProposalRequiredExternalRefRule("lenient incident proposal needs a value"),
            LenientIncidentProposalCodePrefixRule("ACM"),
            LenientIncidentProposalRequiredCodeRule("lenient incident proposal needs an entry"),
            LenientIncidentProposalCountryLengthRule(32),
            LenientIncidentProposalStopCountRangeRule(64, 80),
        ),
    )
}
