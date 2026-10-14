package com.acme.transport.crewing.qualification.cycle

/** A rule a qualification threshold must pass. Check returns its problems, or none. */
interface QualificationThresholdRule {
    fun check(item: QualificationThreshold): MutableList<String>
}

data class QualificationThresholdRegionLengthRule(var maxLength: Int) : QualificationThresholdRule {
    override fun check(item: QualificationThreshold): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationThresholdRequiredRegionRule(var message: String) : QualificationThresholdRule {
    override fun check(item: QualificationThreshold): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationThresholdAttemptsRangeRule(var min: Int, var max: Int) : QualificationThresholdRule {
    override fun check(item: QualificationThreshold): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification threshold. */
data class QualificationThresholdValidator(var rules: MutableList<QualificationThresholdRule>) {
    fun validate(item: QualificationThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationThresholdValidator(): QualificationThresholdValidator {
    return QualificationThresholdValidator(
        mutableListOf<QualificationThresholdRule>(
            QualificationThresholdRegionLengthRule(64),
            QualificationThresholdRequiredRegionRule("qualification threshold needs this field"),
            QualificationThresholdAttemptsRangeRule(3600, 86400),
        ),
    )
}
