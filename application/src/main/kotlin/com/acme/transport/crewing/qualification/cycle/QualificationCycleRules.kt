package com.acme.transport.crewing.qualification.cycle

/** A rule a qualification cycle must pass. Check returns its problems, or none. */
interface QualificationCycleRule {
    fun check(item: QualificationCycle): MutableList<String>
}

data class QualificationCycleTitleLengthRule(var maxLength: Int) : QualificationCycleRule {
    override fun check(item: QualificationCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationCycleRequiredTitleRule(var message: String) : QualificationCycleRule {
    override fun check(item: QualificationCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationCycleOdometerRangeRule(var min: Int, var max: Int) : QualificationCycleRule {
    override fun check(item: QualificationCycle): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a qualification cycle. */
data class QualificationCycleValidator(var rules: MutableList<QualificationCycleRule>) {
    fun validate(item: QualificationCycle): MutableList<String> {
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

    fun requireValid(item: QualificationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationCycleValidator(): QualificationCycleValidator {
    return QualificationCycleValidator(
        mutableListOf<QualificationCycleRule>(
            QualificationCycleTitleLengthRule(120),
            QualificationCycleRequiredTitleRule("qualification cycle needs this field"),
            QualificationCycleOdometerRangeRule(32, 3600),
        ),
    )
}
