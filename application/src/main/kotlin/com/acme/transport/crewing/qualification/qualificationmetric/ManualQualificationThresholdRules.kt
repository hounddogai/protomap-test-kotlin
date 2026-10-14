package com.acme.transport.crewing.qualification.qualificationmetric

/** A rule a manual qualification threshold must pass. Check returns its problems, or none. */
interface ManualQualificationThresholdRule {
    fun check(item: ManualQualificationThreshold): MutableList<String>
}

data class ManualQualificationThresholdExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualQualificationThresholdRule {
    override fun check(item: ManualQualificationThreshold): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationThresholdRequiredReviewerRule(
    var message: String,
) : ManualQualificationThresholdRule {
    override fun check(item: ManualQualificationThreshold): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationThresholdCodePrefixRule(var prefix: String) : ManualQualificationThresholdRule {
    override fun check(item: ManualQualificationThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualQualificationThresholdCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualQualificationThresholdRule {
    override fun check(item: ManualQualificationThreshold): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual qualification threshold. */
data class ManualQualificationThresholdValidator(var rules: MutableList<ManualQualificationThresholdRule>) {
    fun validate(item: ManualQualificationThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualQualificationThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualQualificationThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultManualQualificationThresholdValidator(): ManualQualificationThresholdValidator {
    return ManualQualificationThresholdValidator(
        mutableListOf<ManualQualificationThresholdRule>(
            ManualQualificationThresholdExpiresAtRangeRule(16, 255),
            ManualQualificationThresholdRequiredReviewerRule("manual qualification threshold needs an entry"),
            ManualQualificationThresholdCodePrefixRule("OPS"),
            ManualQualificationThresholdCreatedAtRangeRule(120, 255),
        ),
    )
}
