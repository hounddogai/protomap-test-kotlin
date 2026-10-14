package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

/** A rule a fuel surcharge tier must pass. Check returns its problems, or none. */
interface FuelSurchargeTierRule {
    fun check(item: FuelSurchargeTier): MutableList<String>
}

data class FuelSurchargeTierLabelLengthRule(var maxLength: Int) : FuelSurchargeTierRule {
    override fun check(item: FuelSurchargeTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeTierRequiredCurrencyRule(var message: String) : FuelSurchargeTierRule {
    override fun check(item: FuelSurchargeTier): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeTierPhoneLengthRule(var maxLength: Int) : FuelSurchargeTierRule {
    override fun check(item: FuelSurchargeTier): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeTierLoadKgRangeRule(var min: Double, var max: Double) : FuelSurchargeTierRule {
    override fun check(item: FuelSurchargeTier): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargeTierRequiredLabelRule(var message: String) : FuelSurchargeTierRule {
    override fun check(item: FuelSurchargeTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge tier. */
data class FuelSurchargeTierValidator(var rules: MutableList<FuelSurchargeTierRule>) {
    fun validate(item: FuelSurchargeTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeTier>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeTierValidator(): FuelSurchargeTierValidator {
    return FuelSurchargeTierValidator(
        mutableListOf<FuelSurchargeTierRule>(
            FuelSurchargeTierLabelLengthRule(3600),
            FuelSurchargeTierRequiredCurrencyRule("fuel surcharge tier needs this field"),
            FuelSurchargeTierPhoneLengthRule(3600),
            FuelSurchargeTierLoadKgRangeRule(2.2, 205.6),
            FuelSurchargeTierRequiredLabelRule("fuel surcharge tier needs an entry"),
        ),
    )
}
