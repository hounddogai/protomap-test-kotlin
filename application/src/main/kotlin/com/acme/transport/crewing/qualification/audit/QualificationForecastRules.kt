package com.acme.transport.crewing.qualification.audit

/** A rule a qualification forecast must pass. Check returns its problems, or none. */
interface QualificationForecastRule {
    fun check(item: QualificationForecast): MutableList<String>
}

data class QualificationForecastRequiredCodeRule(var message: String) : QualificationForecastRule {
    override fun check(item: QualificationForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationForecastRequiredNameRule(var message: String) : QualificationForecastRule {
    override fun check(item: QualificationForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationForecastRequiredTimezoneRule(var message: String) : QualificationForecastRule {
    override fun check(item: QualificationForecast): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification forecast. */
data class QualificationForecastValidator(var rules: MutableList<QualificationForecastRule>) {
    fun validate(item: QualificationForecast): MutableList<String> {
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

    fun requireValid(item: QualificationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationForecastValidator(): QualificationForecastValidator {
    return QualificationForecastValidator(
        mutableListOf<QualificationForecastRule>(
            QualificationForecastRequiredCodeRule("qualification forecast needs an entry"),
            QualificationForecastRequiredNameRule("qualification forecast needs a value"),
            QualificationForecastRequiredTimezoneRule("qualification forecast needs a value"),
        ),
    )
}
