package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a seasonal accessorial proposal must pass. Check returns its problems, or none. */
interface SeasonalAccessorialProposalRule {
    fun check(item: SeasonalAccessorialProposal): MutableList<String>
}

data class SeasonalAccessorialProposalRequiredVehicleIdRule(
    var message: String,
) : SeasonalAccessorialProposalRule {
    override fun check(item: SeasonalAccessorialProposal): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalAccessorialProposalRule {
    override fun check(item: SeasonalAccessorialProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialProposalCodePrefixRule(var prefix: String) : SeasonalAccessorialProposalRule {
    override fun check(item: SeasonalAccessorialProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialProposalRequiredLabelRule(var message: String) : SeasonalAccessorialProposalRule {
    override fun check(item: SeasonalAccessorialProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialProposalRequiredTimezoneRule(
    var message: String,
) : SeasonalAccessorialProposalRule {
    override fun check(item: SeasonalAccessorialProposal): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal accessorial proposal. */
data class SeasonalAccessorialProposalValidator(var rules: MutableList<SeasonalAccessorialProposalRule>) {
    fun validate(item: SeasonalAccessorialProposal): MutableList<String> {
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

    fun requireValid(item: SeasonalAccessorialProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAccessorialProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAccessorialProposalValidator(): SeasonalAccessorialProposalValidator {
    return SeasonalAccessorialProposalValidator(
        mutableListOf<SeasonalAccessorialProposalRule>(
            SeasonalAccessorialProposalRequiredVehicleIdRule("seasonal accessorial proposal needs an entry"),
            SeasonalAccessorialProposalAllowedCodeRule(mutableListOf<String>("seasonal", "tiered", "strict")),
            SeasonalAccessorialProposalCodePrefixRule("OPS"),
            SeasonalAccessorialProposalRequiredLabelRule("seasonal accessorial proposal needs an entry"),
            SeasonalAccessorialProposalRequiredTimezoneRule("seasonal accessorial proposal needs this field"),
        ),
    )
}
