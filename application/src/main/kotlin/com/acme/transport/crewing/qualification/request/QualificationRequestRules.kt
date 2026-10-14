package com.acme.transport.crewing.qualification.request

/** A rule a qualification request must pass. Check returns its problems, or none. */
interface QualificationRequestRule {
    fun check(item: QualificationRequest): MutableList<String>
}

data class QualificationRequestVisibleGuardRule(var limit: Double) : QualificationRequestRule {
    override fun check(item: QualificationRequest): MutableList<String> {
        if (item.visible && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRequestDiscountRangeRule(var min: Double, var max: Double) : QualificationRequestRule {
    override fun check(item: QualificationRequest): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRequestMaximumRangeRule(var min: Double, var max: Double) : QualificationRequestRule {
    override fun check(item: QualificationRequest): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationRequestFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : QualificationRequestRule {
    override fun check(item: QualificationRequest): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class QualificationRequestRequiredCodeRule(var message: String) : QualificationRequestRule {
    override fun check(item: QualificationRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationRequestLabelLengthRule(var maxLength: Int) : QualificationRequestRule {
    override fun check(item: QualificationRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification request. */
data class QualificationRequestValidator(var rules: MutableList<QualificationRequestRule>) {
    fun validate(item: QualificationRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationRequestValidator(): QualificationRequestValidator {
    return QualificationRequestValidator(
        mutableListOf<QualificationRequestRule>(
            QualificationRequestVisibleGuardRule(8.7),
            QualificationRequestDiscountRangeRule(5.6, 2520.7),
            QualificationRequestMaximumRangeRule(9.3, 1616.3),
            QualificationRequestFuelLitersRangeRule(8.1, 2188.6),
            QualificationRequestRequiredCodeRule("qualification request needs an entry"),
            QualificationRequestLabelLengthRule(255),
        ),
    )
}
