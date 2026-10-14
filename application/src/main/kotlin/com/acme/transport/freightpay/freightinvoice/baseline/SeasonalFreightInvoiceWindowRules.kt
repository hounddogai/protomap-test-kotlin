package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.text.normalize

/** A rule a seasonal freight invoice window must pass. Check returns its problems, or none. */
interface SeasonalFreightInvoiceWindowRule {
    fun check(item: SeasonalFreightInvoiceWindow): MutableList<String>
}

data class SeasonalFreightInvoiceWindowRequiredNameRule(
    var message: String,
) : SeasonalFreightInvoiceWindowRule {
    override fun check(item: SeasonalFreightInvoiceWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceWindowEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalFreightInvoiceWindowRule {
    override fun check(item: SeasonalFreightInvoiceWindow): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceWindowAllowedNameRule(
    var allowed: MutableList<String>,
) : SeasonalFreightInvoiceWindowRule {
    override fun check(item: SeasonalFreightInvoiceWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceWindowBudgetRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalFreightInvoiceWindowRule {
    override fun check(item: SeasonalFreightInvoiceWindow): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal freight invoice window. */
data class SeasonalFreightInvoiceWindowValidator(var rules: MutableList<SeasonalFreightInvoiceWindowRule>) {
    fun validate(item: SeasonalFreightInvoiceWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalFreightInvoiceWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalFreightInvoiceWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalFreightInvoiceWindowValidator(): SeasonalFreightInvoiceWindowValidator {
    return SeasonalFreightInvoiceWindowValidator(
        mutableListOf<SeasonalFreightInvoiceWindowRule>(
            SeasonalFreightInvoiceWindowRequiredNameRule("seasonal freight invoice window needs a value"),
            SeasonalFreightInvoiceWindowEndsAtRangeRule(80, 86400),
            SeasonalFreightInvoiceWindowAllowedNameRule(mutableListOf<String>("manual", "manual", "seasonal")),
            SeasonalFreightInvoiceWindowBudgetRangeRule(2.9, 3965.1),
        ),
    )
}
