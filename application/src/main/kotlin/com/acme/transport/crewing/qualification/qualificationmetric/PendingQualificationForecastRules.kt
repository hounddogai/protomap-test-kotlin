package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.text.normalize

/** A rule a pending qualification forecast must pass. Check returns its problems, or none. */
interface PendingQualificationForecastRule {
    fun check(item: PendingQualificationForecast): MutableList<String>
}

data class PendingQualificationForecastRequiredCodeRule(
    var message: String,
) : PendingQualificationForecastRule {
    override fun check(item: PendingQualificationForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationForecastAllowedTitleRule(
    var allowed: MutableList<String>,
) : PendingQualificationForecastRule {
    override fun check(item: PendingQualificationForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationForecastTitleLengthRule(var maxLength: Int) : PendingQualificationForecastRule {
    override fun check(item: PendingQualificationForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending qualification forecast. */
data class PendingQualificationForecastValidator(var rules: MutableList<PendingQualificationForecastRule>) {
    fun validate(item: PendingQualificationForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingQualificationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingQualificationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingQualificationForecastValidator(): PendingQualificationForecastValidator {
    return PendingQualificationForecastValidator(
        mutableListOf<PendingQualificationForecastRule>(
            PendingQualificationForecastRequiredCodeRule("pending qualification forecast needs an entry"),
            PendingQualificationForecastAllowedTitleRule(mutableListOf<String>()),
            PendingQualificationForecastTitleLengthRule(120),
        ),
    )
}
