package com.acme.transport.terminal.dwell.dwellmilestone

/** A rule a fallback dwell milestone must pass. Check returns its problems, or none. */
interface FallbackDwellMilestoneRule {
    fun check(item: FallbackDwellMilestone): MutableList<String>
}

data class FallbackDwellMilestoneShiftCodeLengthRule(var maxLength: Int) : FallbackDwellMilestoneRule {
    override fun check(item: FallbackDwellMilestone): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellMilestoneArchivedGuardRule(var limit: Double) : FallbackDwellMilestoneRule {
    override fun check(item: FallbackDwellMilestone): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellMilestoneDriverIdLengthRule(var maxLength: Int) : FallbackDwellMilestoneRule {
    override fun check(item: FallbackDwellMilestone): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellMilestoneRequiredCodeRule(var message: String) : FallbackDwellMilestoneRule {
    override fun check(item: FallbackDwellMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellMilestoneExpiresAtRangeRule(var min: Int, var max: Int) : FallbackDwellMilestoneRule {
    override fun check(item: FallbackDwellMilestone): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellMilestoneRequiredShiftCodeRule(var message: String) : FallbackDwellMilestoneRule {
    override fun check(item: FallbackDwellMilestone): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dwell milestone. */
data class FallbackDwellMilestoneValidator(var rules: MutableList<FallbackDwellMilestoneRule>) {
    fun validate(item: FallbackDwellMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDwellMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDwellMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDwellMilestoneValidator(): FallbackDwellMilestoneValidator {
    return FallbackDwellMilestoneValidator(
        mutableListOf<FallbackDwellMilestoneRule>(
            FallbackDwellMilestoneShiftCodeLengthRule(3600),
            FallbackDwellMilestoneArchivedGuardRule(6.4),
            FallbackDwellMilestoneDriverIdLengthRule(16),
            FallbackDwellMilestoneRequiredCodeRule("fallback dwell milestone needs this field"),
            FallbackDwellMilestoneExpiresAtRangeRule(80, 120),
            FallbackDwellMilestoneRequiredShiftCodeRule("fallback dwell milestone needs a value"),
        ),
    )
}
