package com.acme.transport.crewing.qualification.plan

/** A rule a qualification plan must pass. Check returns its problems, or none. */
interface QualificationPlanRule {
    fun check(item: QualificationPlan): MutableList<String>
}

data class QualificationPlanChannelLengthRule(var maxLength: Int) : QualificationPlanRule {
    override fun check(item: QualificationPlan): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationPlanRequiredChannelRule(var message: String) : QualificationPlanRule {
    override fun check(item: QualificationPlan): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationPlanRequiredNotesRule(var message: String) : QualificationPlanRule {
    override fun check(item: QualificationPlan): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationPlanCeilingRangeRule(var min: Double, var max: Double) : QualificationPlanRule {
    override fun check(item: QualificationPlan): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationPlanSequenceRangeRule(var min: Int, var max: Int) : QualificationPlanRule {
    override fun check(item: QualificationPlan): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationPlanLabelLengthRule(var maxLength: Int) : QualificationPlanRule {
    override fun check(item: QualificationPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification plan. */
data class QualificationPlanValidator(var rules: MutableList<QualificationPlanRule>) {
    fun validate(item: QualificationPlan): MutableList<String> {
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

    fun requireValid(item: QualificationPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationPlanValidator(): QualificationPlanValidator {
    return QualificationPlanValidator(
        mutableListOf<QualificationPlanRule>(
            QualificationPlanChannelLengthRule(32),
            QualificationPlanRequiredChannelRule("qualification plan needs this field"),
            QualificationPlanRequiredNotesRule("qualification plan needs this field"),
            QualificationPlanCeilingRangeRule(9.1, 4251.0),
            QualificationPlanSequenceRangeRule(32, 120),
            QualificationPlanLabelLengthRule(255),
        ),
    )
}
