package com.acme.transport.freightpay.freightinvoice.invoicemanifest

/** A rule a freight invoice plan must pass. Check returns its problems, or none. */
interface FreightInvoicePlanRule {
    fun check(item: FreightInvoicePlan): MutableList<String>
}

data class FreightInvoicePlanSequenceRangeRule(var min: Int, var max: Int) : FreightInvoicePlanRule {
    override fun check(item: FreightInvoicePlan): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class FreightInvoicePlanStopCountRangeRule(var min: Int, var max: Int) : FreightInvoicePlanRule {
    override fun check(item: FreightInvoicePlan): MutableList<String> {
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

data class FreightInvoicePlanQuantityRangeRule(var min: Int, var max: Int) : FreightInvoicePlanRule {
    override fun check(item: FreightInvoicePlan): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoicePlanRequiredLabelRule(var message: String) : FreightInvoicePlanRule {
    override fun check(item: FreightInvoicePlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice plan. */
data class FreightInvoicePlanValidator(var rules: MutableList<FreightInvoicePlanRule>) {
    fun validate(item: FreightInvoicePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoicePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoicePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoicePlanValidator(): FreightInvoicePlanValidator {
    return FreightInvoicePlanValidator(
        mutableListOf<FreightInvoicePlanRule>(
            FreightInvoicePlanSequenceRangeRule(120, 3600),
            FreightInvoicePlanStopCountRangeRule(16, 3600),
            FreightInvoicePlanQuantityRangeRule(120, 255),
            FreightInvoicePlanRequiredLabelRule("freight invoice plan needs an entry"),
        ),
    )
}
