package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

/** A rule a primary fuel surcharge draft must pass. Check returns its problems, or none. */
interface PrimaryFuelSurchargeDraftRule {
    fun check(item: PrimaryFuelSurchargeDraft): MutableList<String>
}

data class PrimaryFuelSurchargeDraftRequiredShiftCodeRule(var message: String) : PrimaryFuelSurchargeDraftRule {
    override fun check(item: PrimaryFuelSurchargeDraft): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeDraftAmountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryFuelSurchargeDraftRule {
    override fun check(item: PrimaryFuelSurchargeDraft): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFuelSurchargeDraftCurrencyLengthRule(var maxLength: Int) : PrimaryFuelSurchargeDraftRule {
    override fun check(item: PrimaryFuelSurchargeDraft): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary fuel surcharge draft. */
data class PrimaryFuelSurchargeDraftValidator(var rules: MutableList<PrimaryFuelSurchargeDraftRule>) {
    fun validate(item: PrimaryFuelSurchargeDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryFuelSurchargeDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFuelSurchargeDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFuelSurchargeDraftValidator(): PrimaryFuelSurchargeDraftValidator {
    return PrimaryFuelSurchargeDraftValidator(
        mutableListOf<PrimaryFuelSurchargeDraftRule>(
            PrimaryFuelSurchargeDraftRequiredShiftCodeRule("primary fuel surcharge draft needs a value"),
            PrimaryFuelSurchargeDraftAmountRangeRule(6.2, 4701.9),
            PrimaryFuelSurchargeDraftCurrencyLengthRule(32),
        ),
    )
}
