package com.acme.transport.crewing.qualification.qualificationdigest

/** A rule a qualification window must pass. Check returns its problems, or none. */
interface QualificationWindowRule {
    fun check(item: QualificationWindow): MutableList<String>
}

data class QualificationWindowCodeLengthRule(var maxLength: Int) : QualificationWindowRule {
    override fun check(item: QualificationWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationWindowBudgetRangeRule(var min: Double, var max: Double) : QualificationWindowRule {
    override fun check(item: QualificationWindow): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class QualificationWindowGraceDaysRangeRule(var min: Int, var max: Int) : QualificationWindowRule {
    override fun check(item: QualificationWindow): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationWindowCodePrefixRule(var prefix: String) : QualificationWindowRule {
    override fun check(item: QualificationWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification window. */
data class QualificationWindowValidator(var rules: MutableList<QualificationWindowRule>) {
    fun validate(item: QualificationWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationWindowValidator(): QualificationWindowValidator {
    return QualificationWindowValidator(
        mutableListOf<QualificationWindowRule>(
            QualificationWindowCodeLengthRule(255),
            QualificationWindowBudgetRangeRule(7.9, 4260.2),
            QualificationWindowGraceDaysRangeRule(80, 120),
            QualificationWindowCodePrefixRule("OPS"),
        ),
    )
}
