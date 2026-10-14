package com.acme.transport.crewing.qualification.bundle

/** A rule a pending qualification estimate must pass. Check returns its problems, or none. */
interface PendingQualificationEstimateRule {
    fun check(item: PendingQualificationEstimate): MutableList<String>
}

data class PendingQualificationEstimateDiscountRangeRule(
    var min: Double,
    var max: Double,
) : PendingQualificationEstimateRule {
    override fun check(item: PendingQualificationEstimate): MutableList<String> {
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

data class PendingQualificationEstimateRequiredLabelRule(
    var message: String,
) : PendingQualificationEstimateRule {
    override fun check(item: PendingQualificationEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationEstimateLabelLengthRule(var maxLength: Int) : PendingQualificationEstimateRule {
    override fun check(item: PendingQualificationEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationEstimateQuantityRangeRule(
    var min: Int,
    var max: Int,
) : PendingQualificationEstimateRule {
    override fun check(item: PendingQualificationEstimate): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending qualification estimate. */
data class PendingQualificationEstimateValidator(var rules: MutableList<PendingQualificationEstimateRule>) {
    fun validate(item: PendingQualificationEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingQualificationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingQualificationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingQualificationEstimateValidator(): PendingQualificationEstimateValidator {
    return PendingQualificationEstimateValidator(
        mutableListOf<PendingQualificationEstimateRule>(
            PendingQualificationEstimateDiscountRangeRule(2.2, 2833.3),
            PendingQualificationEstimateRequiredLabelRule("pending qualification estimate needs a value"),
            PendingQualificationEstimateLabelLengthRule(255),
            PendingQualificationEstimateQuantityRangeRule(32, 86400),
        ),
    )
}
