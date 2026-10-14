package com.acme.transport.crewing.qualification.estimate

/** A rule a qualification estimate must pass. Check returns its problems, or none. */
interface QualificationEstimateRule {
    fun check(item: QualificationEstimate): MutableList<String>
}

data class QualificationEstimateRequiredLabelRule(var message: String) : QualificationEstimateRule {
    override fun check(item: QualificationEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationEstimateMaximumRangeRule(var min: Double, var max: Double) : QualificationEstimateRule {
    override fun check(item: QualificationEstimate): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationEstimateLabelLengthRule(var maxLength: Int) : QualificationEstimateRule {
    override fun check(item: QualificationEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationEstimatePalletsRangeRule(var min: Int, var max: Int) : QualificationEstimateRule {
    override fun check(item: QualificationEstimate): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationEstimateRequiredLocaleRule(var message: String) : QualificationEstimateRule {
    override fun check(item: QualificationEstimate): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationEstimateCodeLengthRule(var maxLength: Int) : QualificationEstimateRule {
    override fun check(item: QualificationEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification estimate. */
data class QualificationEstimateValidator(var rules: MutableList<QualificationEstimateRule>) {
    fun validate(item: QualificationEstimate): MutableList<String> {
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

    fun requireValid(item: QualificationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationEstimateValidator(): QualificationEstimateValidator {
    return QualificationEstimateValidator(
        mutableListOf<QualificationEstimateRule>(
            QualificationEstimateRequiredLabelRule("qualification estimate needs this field"),
            QualificationEstimateMaximumRangeRule(3.3, 4491.1),
            QualificationEstimateLabelLengthRule(86400),
            QualificationEstimatePalletsRangeRule(255, 86400),
            QualificationEstimateRequiredLocaleRule("qualification estimate needs a value"),
            QualificationEstimateCodeLengthRule(64),
        ),
    )
}
