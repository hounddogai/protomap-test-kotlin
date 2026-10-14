package com.acme.transport.freightpay.fuelsurcharge.template

/** A rule a seasonal fuel surcharge batch must pass. Check returns its problems, or none. */
interface SeasonalFuelSurchargeBatchRule {
    fun check(item: SeasonalFuelSurchargeBatch): MutableList<String>
}

data class SeasonalFuelSurchargeBatchRequiredLabelRule(var message: String) : SeasonalFuelSurchargeBatchRule {
    override fun check(item: SeasonalFuelSurchargeBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeBatchNameLengthRule(var maxLength: Int) : SeasonalFuelSurchargeBatchRule {
    override fun check(item: SeasonalFuelSurchargeBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFuelSurchargeBatchRequiredNameRule(var message: String) : SeasonalFuelSurchargeBatchRule {
    override fun check(item: SeasonalFuelSurchargeBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal fuel surcharge batch. */
data class SeasonalFuelSurchargeBatchValidator(var rules: MutableList<SeasonalFuelSurchargeBatchRule>) {
    fun validate(item: SeasonalFuelSurchargeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalFuelSurchargeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalFuelSurchargeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalFuelSurchargeBatchValidator(): SeasonalFuelSurchargeBatchValidator {
    return SeasonalFuelSurchargeBatchValidator(
        mutableListOf<SeasonalFuelSurchargeBatchRule>(
            SeasonalFuelSurchargeBatchRequiredLabelRule("seasonal fuel surcharge batch needs this field"),
            SeasonalFuelSurchargeBatchNameLengthRule(80),
            SeasonalFuelSurchargeBatchRequiredNameRule("seasonal fuel surcharge batch needs an entry"),
        ),
    )
}
