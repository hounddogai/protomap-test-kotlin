package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.kit.text.normalize

/** A rule a regional tire proposal must pass. Check returns its problems, or none. */
interface RegionalTireProposalRule {
    fun check(item: RegionalTireProposal): MutableList<String>
}

data class RegionalTireProposalCodePrefixRule(var prefix: String) : RegionalTireProposalRule {
    override fun check(item: RegionalTireProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTireProposalDiscountRangeRule(var min: Double, var max: Double) : RegionalTireProposalRule {
    override fun check(item: RegionalTireProposal): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTireProposalChecksumLengthRule(var maxLength: Int) : RegionalTireProposalRule {
    override fun check(item: RegionalTireProposal): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTireProposalNameLengthRule(var maxLength: Int) : RegionalTireProposalRule {
    override fun check(item: RegionalTireProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTireProposalRequiredCodeRule(var message: String) : RegionalTireProposalRule {
    override fun check(item: RegionalTireProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTireProposalAllowedNameRule(var allowed: MutableList<String>) : RegionalTireProposalRule {
    override fun check(item: RegionalTireProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional tire proposal. */
data class RegionalTireProposalValidator(var rules: MutableList<RegionalTireProposalRule>) {
    fun validate(item: RegionalTireProposal): MutableList<String> {
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

    fun requireValid(item: RegionalTireProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTireProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTireProposalValidator(): RegionalTireProposalValidator {
    return RegionalTireProposalValidator(
        mutableListOf<RegionalTireProposalRule>(
            RegionalTireProposalCodePrefixRule("OPS"),
            RegionalTireProposalDiscountRangeRule(0.7, 737.8),
            RegionalTireProposalChecksumLengthRule(16),
            RegionalTireProposalNameLengthRule(80),
            RegionalTireProposalRequiredCodeRule("regional tire proposal needs a value"),
            RegionalTireProposalAllowedNameRule(mutableListOf<String>("primary", "global")),
        ),
    )
}
