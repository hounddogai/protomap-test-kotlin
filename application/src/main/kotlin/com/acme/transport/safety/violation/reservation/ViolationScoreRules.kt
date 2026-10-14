package com.acme.transport.safety.violation.reservation

/** A rule a violation score must pass. Check returns its problems, or none. */
interface ViolationScoreRule {
    fun check(item: ViolationScore): MutableList<String>
}

data class ViolationScorePinnedGuardRule(var limit: Int) : ViolationScoreRule {
    override fun check(item: ViolationScore): MutableList<String> {
        if (item.pinned && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ViolationScoreRequiredLabelRule(var message: String) : ViolationScoreRule {
    override fun check(item: ViolationScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationScoreOdometerRangeRule(var min: Int, var max: Int) : ViolationScoreRule {
    override fun check(item: ViolationScore): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationScoreVersionRangeRule(var min: Int, var max: Int) : ViolationScoreRule {
    override fun check(item: ViolationScore): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation score. */
data class ViolationScoreValidator(var rules: MutableList<ViolationScoreRule>) {
    fun validate(item: ViolationScore): MutableList<String> {
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

    fun requireValid(item: ViolationScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationScore>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationScoreValidator(): ViolationScoreValidator {
    return ViolationScoreValidator(
        mutableListOf<ViolationScoreRule>(
            ViolationScorePinnedGuardRule(3600),
            ViolationScoreRequiredLabelRule("violation score needs this field"),
            ViolationScoreOdometerRangeRule(80, 80),
            ViolationScoreVersionRangeRule(64, 3600),
        ),
    )
}
