package com.acme.transport.freightpay.freightinvoice.preference

/** A rule a freight invoice budget must pass. Check returns its problems, or none. */
interface FreightInvoiceBudgetRule {
    fun check(item: FreightInvoiceBudget): MutableList<String>
}

data class FreightInvoiceBudgetRequiredCarrierRule(var message: String) : FreightInvoiceBudgetRule {
    override fun check(item: FreightInvoiceBudget): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBudgetOdometerRangeRule(var min: Int, var max: Int) : FreightInvoiceBudgetRule {
    override fun check(item: FreightInvoiceBudget): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceBudgetCodeLengthRule(var maxLength: Int) : FreightInvoiceBudgetRule {
    override fun check(item: FreightInvoiceBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBudgetUnitLengthRule(var maxLength: Int) : FreightInvoiceBudgetRule {
    override fun check(item: FreightInvoiceBudget): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice budget. */
data class FreightInvoiceBudgetValidator(var rules: MutableList<FreightInvoiceBudgetRule>) {
    fun validate(item: FreightInvoiceBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceBudgetValidator(): FreightInvoiceBudgetValidator {
    return FreightInvoiceBudgetValidator(
        mutableListOf<FreightInvoiceBudgetRule>(
            FreightInvoiceBudgetRequiredCarrierRule("freight invoice budget needs this field"),
            FreightInvoiceBudgetOdometerRangeRule(255, 255),
            FreightInvoiceBudgetCodeLengthRule(32),
            FreightInvoiceBudgetUnitLengthRule(120),
        ),
    )
}
