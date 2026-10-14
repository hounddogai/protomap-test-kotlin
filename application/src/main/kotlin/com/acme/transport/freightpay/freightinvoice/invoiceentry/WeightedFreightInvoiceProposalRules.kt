package com.acme.transport.freightpay.freightinvoice.invoiceentry

/** A rule a weighted freight invoice proposal must pass. Check returns its problems, or none. */
interface WeightedFreightInvoiceProposalRule {
    fun check(item: WeightedFreightInvoiceProposal): MutableList<String>
}

data class WeightedFreightInvoiceProposalQuantityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFreightInvoiceProposalRule {
    override fun check(item: WeightedFreightInvoiceProposal): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFreightInvoiceProposalPalletsRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFreightInvoiceProposalRule {
    override fun check(item: WeightedFreightInvoiceProposal): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class WeightedFreightInvoiceProposalVersionRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFreightInvoiceProposalRule {
    override fun check(item: WeightedFreightInvoiceProposal): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class WeightedFreightInvoiceProposalRequiredTitleRule(
    var message: String,
) : WeightedFreightInvoiceProposalRule {
    override fun check(item: WeightedFreightInvoiceProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted freight invoice proposal. */
data class WeightedFreightInvoiceProposalValidator(var rules: MutableList<WeightedFreightInvoiceProposalRule>) {
    fun validate(item: WeightedFreightInvoiceProposal): MutableList<String> {
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

    fun requireValid(item: WeightedFreightInvoiceProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFreightInvoiceProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFreightInvoiceProposalValidator(): WeightedFreightInvoiceProposalValidator {
    return WeightedFreightInvoiceProposalValidator(
        mutableListOf<WeightedFreightInvoiceProposalRule>(
            WeightedFreightInvoiceProposalQuantityRangeRule(32, 86400),
            WeightedFreightInvoiceProposalPalletsRangeRule(120, 86400),
            WeightedFreightInvoiceProposalVersionRangeRule(120, 255),
            WeightedFreightInvoiceProposalRequiredTitleRule("weighted freight invoice proposal needs an entry"),
        ),
    )
}
