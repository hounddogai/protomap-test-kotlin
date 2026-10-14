package com.acme.transport.crewing.qualification.estimate

/** A rule a qualification exception must pass. Check returns its problems, or none. */
interface QualificationExceptionRule {
    fun check(item: QualificationException): MutableList<String>
}

data class QualificationExceptionRequiredPhoneRule(var message: String) : QualificationExceptionRule {
    override fun check(item: QualificationException): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationExceptionRatioRangeRule(var min: Double, var max: Double) : QualificationExceptionRule {
    override fun check(item: QualificationException): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class QualificationExceptionRequiredCodeRule(var message: String) : QualificationExceptionRule {
    override fun check(item: QualificationException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationExceptionRequiredTitleRule(var message: String) : QualificationExceptionRule {
    override fun check(item: QualificationException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification exception. */
data class QualificationExceptionValidator(var rules: MutableList<QualificationExceptionRule>) {
    fun validate(item: QualificationException): MutableList<String> {
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

    fun requireValid(item: QualificationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationException>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationExceptionValidator(): QualificationExceptionValidator {
    return QualificationExceptionValidator(
        mutableListOf<QualificationExceptionRule>(
            QualificationExceptionRequiredPhoneRule("qualification exception needs an entry"),
            QualificationExceptionRatioRangeRule(5.3, 4252.8),
            QualificationExceptionRequiredCodeRule("qualification exception needs an entry"),
            QualificationExceptionRequiredTitleRule("qualification exception needs an entry"),
        ),
    )
}
