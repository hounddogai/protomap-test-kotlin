package com.acme.transport.crewing.qualification.qualificationdigest

/** A rule a legacy qualification policy must pass. Check returns its problems, or none. */
interface LegacyQualificationPolicyRule {
    fun check(item: LegacyQualificationPolicy): MutableList<String>
}

data class LegacyQualificationPolicyPriorityRangeRule(
    var min: Int,
    var max: Int,
) : LegacyQualificationPolicyRule {
    override fun check(item: LegacyQualificationPolicy): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class LegacyQualificationPolicyNameLengthRule(var maxLength: Int) : LegacyQualificationPolicyRule {
    override fun check(item: LegacyQualificationPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyQualificationPolicyRequiredCodeRule(var message: String) : LegacyQualificationPolicyRule {
    override fun check(item: LegacyQualificationPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy qualification policy. */
data class LegacyQualificationPolicyValidator(var rules: MutableList<LegacyQualificationPolicyRule>) {
    fun validate(item: LegacyQualificationPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyQualificationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyQualificationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyQualificationPolicyValidator(): LegacyQualificationPolicyValidator {
    return LegacyQualificationPolicyValidator(
        mutableListOf<LegacyQualificationPolicyRule>(
            LegacyQualificationPolicyPriorityRangeRule(1000, 86400),
            LegacyQualificationPolicyNameLengthRule(86400),
            LegacyQualificationPolicyRequiredCodeRule("legacy qualification policy needs this field"),
        ),
    )
}
