package com.acme.transport.emissions.fuelburn.variance

/** A rule a fuel burn agreement must pass. Check returns its problems, or none. */
interface FuelBurnAgreementRule {
    fun check(item: FuelBurnAgreement): MutableList<String>
}

data class FuelBurnAgreementTitleLengthRule(var maxLength: Int) : FuelBurnAgreementRule {
    override fun check(item: FuelBurnAgreement): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAgreementDiscountRangeRule(var min: Double, var max: Double) : FuelBurnAgreementRule {
    override fun check(item: FuelBurnAgreement): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class FuelBurnAgreementRequiredDockRule(var message: String) : FuelBurnAgreementRule {
    override fun check(item: FuelBurnAgreement): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn agreement. */
data class FuelBurnAgreementValidator(var rules: MutableList<FuelBurnAgreementRule>) {
    fun validate(item: FuelBurnAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnAgreementValidator(): FuelBurnAgreementValidator {
    return FuelBurnAgreementValidator(
        mutableListOf<FuelBurnAgreementRule>(
            FuelBurnAgreementTitleLengthRule(86400),
            FuelBurnAgreementDiscountRangeRule(3.4, 2080.9),
            FuelBurnAgreementRequiredDockRule("fuel burn agreement needs this field"),
        ),
    )
}
