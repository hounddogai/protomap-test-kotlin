package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.normalize

/** A rule a rolling idling proposal must pass. Check returns its problems, or none. */
interface RollingIdlingProposalRule {
    fun check(item: RollingIdlingProposal): MutableList<String>
}

data class RollingIdlingProposalVehicleIdLengthRule(var maxLength: Int) : RollingIdlingProposalRule {
    override fun check(item: RollingIdlingProposal): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingProposalCodePrefixRule(var prefix: String) : RollingIdlingProposalRule {
    override fun check(item: RollingIdlingProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingProposalAllowedNameRule(var allowed: MutableList<String>) : RollingIdlingProposalRule {
    override fun check(item: RollingIdlingProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingProposalCodeLengthRule(var maxLength: Int) : RollingIdlingProposalRule {
    override fun check(item: RollingIdlingProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingIdlingProposalRequiredNameRule(var message: String) : RollingIdlingProposalRule {
    override fun check(item: RollingIdlingProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling idling proposal. */
data class RollingIdlingProposalValidator(var rules: MutableList<RollingIdlingProposalRule>) {
    fun validate(item: RollingIdlingProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingIdlingProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingIdlingProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingIdlingProposalValidator(): RollingIdlingProposalValidator {
    return RollingIdlingProposalValidator(
        mutableListOf<RollingIdlingProposalRule>(
            RollingIdlingProposalVehicleIdLengthRule(255),
            RollingIdlingProposalCodePrefixRule("REF"),
            RollingIdlingProposalAllowedNameRule(mutableListOf<String>("fallback")),
            RollingIdlingProposalCodeLengthRule(3600),
            RollingIdlingProposalRequiredNameRule("rolling idling proposal needs an entry"),
        ),
    )
}
