package com.acme.transport.crewing.qualification.milestone

/** A rule a qualification milestone must pass. Check returns its problems, or none. */
interface QualificationMilestoneRule {
    fun check(item: QualificationMilestone): MutableList<String>
}

data class QualificationMilestoneCodePrefixRule(var prefix: String) : QualificationMilestoneRule {
    override fun check(item: QualificationMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMilestoneNameLengthRule(var maxLength: Int) : QualificationMilestoneRule {
    override fun check(item: QualificationMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMilestoneCodeLengthRule(var maxLength: Int) : QualificationMilestoneRule {
    override fun check(item: QualificationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationMilestoneEndsAtRangeRule(var min: Int, var max: Int) : QualificationMilestoneRule {
    override fun check(item: QualificationMilestone): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification milestone. */
data class QualificationMilestoneValidator(var rules: MutableList<QualificationMilestoneRule>) {
    fun validate(item: QualificationMilestone): MutableList<String> {
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

    fun requireValid(item: QualificationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationMilestoneValidator(): QualificationMilestoneValidator {
    return QualificationMilestoneValidator(
        mutableListOf<QualificationMilestoneRule>(
            QualificationMilestoneCodePrefixRule("REF"),
            QualificationMilestoneNameLengthRule(80),
            QualificationMilestoneCodeLengthRule(1000),
            QualificationMilestoneEndsAtRangeRule(1000, 86400),
        ),
    )
}
