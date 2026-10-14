package com.acme.transport.crewing.qualification.preference

/** A rule a qualification preference must pass. Check returns its problems, or none. */
interface QualificationPreferenceRule {
    fun check(item: QualificationPreference): MutableList<String>
}

data class QualificationPreferenceRequiredLabelRule(var message: String) : QualificationPreferenceRule {
    override fun check(item: QualificationPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationPreferenceThresholdRangeRule(
    var min: Double,
    var max: Double,
) : QualificationPreferenceRule {
    override fun check(item: QualificationPreference): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationPreferenceExpiresAtRangeRule(var min: Int, var max: Int) : QualificationPreferenceRule {
    override fun check(item: QualificationPreference): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification preference. */
data class QualificationPreferenceValidator(var rules: MutableList<QualificationPreferenceRule>) {
    fun validate(item: QualificationPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationPreferenceValidator(): QualificationPreferenceValidator {
    return QualificationPreferenceValidator(
        mutableListOf<QualificationPreferenceRule>(
            QualificationPreferenceRequiredLabelRule("qualification preference needs a value"),
            QualificationPreferenceThresholdRangeRule(6.1, 1871.9),
            QualificationPreferenceExpiresAtRangeRule(16, 120),
        ),
    )
}
