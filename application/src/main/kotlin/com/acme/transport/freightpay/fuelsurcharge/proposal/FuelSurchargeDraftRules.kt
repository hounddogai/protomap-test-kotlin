package com.acme.transport.freightpay.fuelsurcharge.proposal

/** A rule a fuel surcharge draft must pass. Check returns its problems, or none. */
interface FuelSurchargeDraftRule {
    fun check(item: FuelSurchargeDraft): MutableList<String>
}

data class FuelSurchargeDraftThresholdRangeRule(var min: Double, var max: Double) : FuelSurchargeDraftRule {
    override fun check(item: FuelSurchargeDraft): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeDraftCreatedAtRangeRule(var min: Int, var max: Int) : FuelSurchargeDraftRule {
    override fun check(item: FuelSurchargeDraft): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargeDraftRequiredLabelRule(var message: String) : FuelSurchargeDraftRule {
    override fun check(item: FuelSurchargeDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge draft. */
data class FuelSurchargeDraftValidator(var rules: MutableList<FuelSurchargeDraftRule>) {
    fun validate(item: FuelSurchargeDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeDraftValidator(): FuelSurchargeDraftValidator {
    return FuelSurchargeDraftValidator(
        mutableListOf<FuelSurchargeDraftRule>(
            FuelSurchargeDraftThresholdRangeRule(1.6, 549.6),
            FuelSurchargeDraftCreatedAtRangeRule(16, 32),
            FuelSurchargeDraftRequiredLabelRule("fuel surcharge draft needs an entry"),
        ),
    )
}
