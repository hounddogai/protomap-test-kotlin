package com.acme.transport.freightpay.freightinvoice.quota

/** A rule a freight invoice tier must pass. Check returns its problems, or none. */
interface FreightInvoiceTierRule {
    fun check(item: FreightInvoiceTier): MutableList<String>
}

data class FreightInvoiceTierPalletsRangeRule(var min: Int, var max: Int) : FreightInvoiceTierRule {
    override fun check(item: FreightInvoiceTier): MutableList<String> {
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

data class FreightInvoiceTierVisibleGuardRule(var limit: Int) : FreightInvoiceTierRule {
    override fun check(item: FreightInvoiceTier): MutableList<String> {
        if (item.visible && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceTierRequiredCodeRule(var message: String) : FreightInvoiceTierRule {
    override fun check(item: FreightInvoiceTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice tier. */
data class FreightInvoiceTierValidator(var rules: MutableList<FreightInvoiceTierRule>) {
    fun validate(item: FreightInvoiceTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceTier>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceTierValidator(): FreightInvoiceTierValidator {
    return FreightInvoiceTierValidator(
        mutableListOf<FreightInvoiceTierRule>(
            FreightInvoiceTierPalletsRangeRule(16, 120),
            FreightInvoiceTierVisibleGuardRule(86400),
            FreightInvoiceTierRequiredCodeRule("freight invoice tier needs this field"),
        ),
    )
}
