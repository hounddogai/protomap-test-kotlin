package com.acme.transport.crewing.qualification.overrideitem

/** A rule a qualification policy must pass. Check returns its problems, or none. */
interface QualificationPolicyRule {
    fun check(item: QualificationPolicy): MutableList<String>
}

data class QualificationPolicyLabelLengthRule(var maxLength: Int) : QualificationPolicyRule {
    override fun check(item: QualificationPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationPolicyCeilingRangeRule(var min: Double, var max: Double) : QualificationPolicyRule {
    override fun check(item: QualificationPolicy): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class QualificationPolicyRequiredDriverIdRule(var message: String) : QualificationPolicyRule {
    override fun check(item: QualificationPolicy): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationPolicyRequiredCodeRule(var message: String) : QualificationPolicyRule {
    override fun check(item: QualificationPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification policy. */
data class QualificationPolicyValidator(var rules: MutableList<QualificationPolicyRule>) {
    fun validate(item: QualificationPolicy): MutableList<String> {
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

    fun requireValid(item: QualificationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationPolicyValidator(): QualificationPolicyValidator {
    return QualificationPolicyValidator(
        mutableListOf<QualificationPolicyRule>(
            QualificationPolicyLabelLengthRule(255),
            QualificationPolicyCeilingRangeRule(8.3, 4926.4),
            QualificationPolicyRequiredDriverIdRule("qualification policy needs this field"),
            QualificationPolicyRequiredCodeRule("qualification policy needs a value"),
        ),
    )
}
