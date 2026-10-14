package com.acme.transport.emissions.carbonreport.variance

/** A rule a weighted carbon report claim must pass. Check returns its problems, or none. */
interface WeightedCarbonReportClaimRule {
    fun check(item: WeightedCarbonReportClaim): MutableList<String>
}

data class WeightedCarbonReportClaimRequiredLabelRule(var message: String) : WeightedCarbonReportClaimRule {
    override fun check(item: WeightedCarbonReportClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportClaimCodePrefixRule(var prefix: String) : WeightedCarbonReportClaimRule {
    override fun check(item: WeightedCarbonReportClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportClaimLabelLengthRule(var maxLength: Int) : WeightedCarbonReportClaimRule {
    override fun check(item: WeightedCarbonReportClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportClaimCurrencyLengthRule(var maxLength: Int) : WeightedCarbonReportClaimRule {
    override fun check(item: WeightedCarbonReportClaim): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCarbonReportClaimRequiredNameRule(var message: String) : WeightedCarbonReportClaimRule {
    override fun check(item: WeightedCarbonReportClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted carbon report claim. */
data class WeightedCarbonReportClaimValidator(var rules: MutableList<WeightedCarbonReportClaimRule>) {
    fun validate(item: WeightedCarbonReportClaim): MutableList<String> {
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

    fun requireValid(item: WeightedCarbonReportClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCarbonReportClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCarbonReportClaimValidator(): WeightedCarbonReportClaimValidator {
    return WeightedCarbonReportClaimValidator(
        mutableListOf<WeightedCarbonReportClaimRule>(
            WeightedCarbonReportClaimRequiredLabelRule("weighted carbon report claim needs an entry"),
            WeightedCarbonReportClaimCodePrefixRule("OPS"),
            WeightedCarbonReportClaimLabelLengthRule(32),
            WeightedCarbonReportClaimCurrencyLengthRule(32),
            WeightedCarbonReportClaimRequiredNameRule("weighted carbon report claim needs a value"),
        ),
    )
}
