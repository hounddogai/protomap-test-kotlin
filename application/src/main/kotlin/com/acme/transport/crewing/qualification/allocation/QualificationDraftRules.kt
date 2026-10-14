package com.acme.transport.crewing.qualification.allocation

/** A rule a qualification draft must pass. Check returns its problems, or none. */
interface QualificationDraftRule {
    fun check(item: QualificationDraft): MutableList<String>
}

data class QualificationDraftDistanceKmRangeRule(var min: Double, var max: Double) : QualificationDraftRule {
    override fun check(item: QualificationDraft): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class QualificationDraftRevisionRangeRule(var min: Int, var max: Int) : QualificationDraftRule {
    override fun check(item: QualificationDraft): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationDraftLabelLengthRule(var maxLength: Int) : QualificationDraftRule {
    override fun check(item: QualificationDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationDraftRequiredCodeRule(var message: String) : QualificationDraftRule {
    override fun check(item: QualificationDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationDraftEtaMinutesRangeRule(var min: Int, var max: Int) : QualificationDraftRule {
    override fun check(item: QualificationDraft): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationDraftCodeLengthRule(var maxLength: Int) : QualificationDraftRule {
    override fun check(item: QualificationDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification draft. */
data class QualificationDraftValidator(var rules: MutableList<QualificationDraftRule>) {
    fun validate(item: QualificationDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationDraftValidator(): QualificationDraftValidator {
    return QualificationDraftValidator(
        mutableListOf<QualificationDraftRule>(
            QualificationDraftDistanceKmRangeRule(4.6, 4445.2),
            QualificationDraftRevisionRangeRule(16, 64),
            QualificationDraftLabelLengthRule(255),
            QualificationDraftRequiredCodeRule("qualification draft needs an entry"),
            QualificationDraftEtaMinutesRangeRule(16, 120),
            QualificationDraftCodeLengthRule(1000),
        ),
    )
}
