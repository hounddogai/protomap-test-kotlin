package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.text.normalize

/** A rule a seasonal freight invoice proposal must pass. Check returns its problems, or none. */
interface SeasonalFreightInvoiceProposalRule {
    fun check(item: SeasonalFreightInvoiceProposal): MutableList<String>
}

data class SeasonalFreightInvoiceProposalLabelLengthRule(
    var maxLength: Int,
) : SeasonalFreightInvoiceProposalRule {
    override fun check(item: SeasonalFreightInvoiceProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceProposalRequiredDescriptionRule(
    var message: String,
) : SeasonalFreightInvoiceProposalRule {
    override fun check(item: SeasonalFreightInvoiceProposal): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceProposalAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalFreightInvoiceProposalRule {
    override fun check(item: SeasonalFreightInvoiceProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceProposalGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalFreightInvoiceProposalRule {
    override fun check(item: SeasonalFreightInvoiceProposal): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal freight invoice proposal. */
data class SeasonalFreightInvoiceProposalValidator(var rules: MutableList<SeasonalFreightInvoiceProposalRule>) {
    fun validate(item: SeasonalFreightInvoiceProposal): MutableList<String> {
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

    fun requireValid(item: SeasonalFreightInvoiceProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalFreightInvoiceProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalFreightInvoiceProposalValidator(): SeasonalFreightInvoiceProposalValidator {
    return SeasonalFreightInvoiceProposalValidator(
        mutableListOf<SeasonalFreightInvoiceProposalRule>(
            SeasonalFreightInvoiceProposalLabelLengthRule(32),
            SeasonalFreightInvoiceProposalRequiredDescriptionRule(
                "seasonal freight invoice proposal needs this field",
            ),
            SeasonalFreightInvoiceProposalAllowedLabelRule(mutableListOf<String>("tiered", "weighted")),
            SeasonalFreightInvoiceProposalGraceDaysRangeRule(64, 3600),
        ),
    )
}
