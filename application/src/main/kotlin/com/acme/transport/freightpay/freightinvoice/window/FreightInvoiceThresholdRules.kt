package com.acme.transport.freightpay.freightinvoice.window

/** A rule a freight invoice threshold must pass. Check returns its problems, or none. */
interface FreightInvoiceThresholdRule {
    fun check(item: FreightInvoiceThreshold): MutableList<String>
}

data class FreightInvoiceThresholdPalletsRangeRule(var min: Int, var max: Int) : FreightInvoiceThresholdRule {
    override fun check(item: FreightInvoiceThreshold): MutableList<String> {
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

data class FreightInvoiceThresholdScoreRangeRule(
    var min: Double,
    var max: Double,
) : FreightInvoiceThresholdRule {
    override fun check(item: FreightInvoiceThreshold): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceThresholdExpiresAtRangeRule(var min: Int, var max: Int) : FreightInvoiceThresholdRule {
    override fun check(item: FreightInvoiceThreshold): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice threshold. */
data class FreightInvoiceThresholdValidator(var rules: MutableList<FreightInvoiceThresholdRule>) {
    fun validate(item: FreightInvoiceThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceThresholdValidator(): FreightInvoiceThresholdValidator {
    return FreightInvoiceThresholdValidator(
        mutableListOf<FreightInvoiceThresholdRule>(
            FreightInvoiceThresholdPalletsRangeRule(64, 86400),
            FreightInvoiceThresholdScoreRangeRule(1.7, 1700.4),
            FreightInvoiceThresholdExpiresAtRangeRule(255, 3600),
        ),
    )
}
