package com.acme.transport.safety.coaching.schedule

/** A rule a coaching plan must pass. Check returns its problems, or none. */
interface CoachingPlanRule {
    fun check(item: CoachingPlan): MutableList<String>
}

data class CoachingPlanOwnerIdLengthRule(var maxLength: Int) : CoachingPlanRule {
    override fun check(item: CoachingPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingPlanNameLengthRule(var maxLength: Int) : CoachingPlanRule {
    override fun check(item: CoachingPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingPlanRequiredChecksumRule(var message: String) : CoachingPlanRule {
    override fun check(item: CoachingPlan): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingPlanCodePrefixRule(var prefix: String) : CoachingPlanRule {
    override fun check(item: CoachingPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingPlanChecksumLengthRule(var maxLength: Int) : CoachingPlanRule {
    override fun check(item: CoachingPlan): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingPlanRequiredNameRule(var message: String) : CoachingPlanRule {
    override fun check(item: CoachingPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching plan. */
data class CoachingPlanValidator(var rules: MutableList<CoachingPlanRule>) {
    fun validate(item: CoachingPlan): MutableList<String> {
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

    fun requireValid(item: CoachingPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingPlanValidator(): CoachingPlanValidator {
    return CoachingPlanValidator(
        mutableListOf<CoachingPlanRule>(
            CoachingPlanOwnerIdLengthRule(16),
            CoachingPlanNameLengthRule(64),
            CoachingPlanRequiredChecksumRule("coaching plan needs this field"),
            CoachingPlanCodePrefixRule("REF"),
            CoachingPlanChecksumLengthRule(120),
            CoachingPlanRequiredNameRule("coaching plan needs a value"),
        ),
    )
}
