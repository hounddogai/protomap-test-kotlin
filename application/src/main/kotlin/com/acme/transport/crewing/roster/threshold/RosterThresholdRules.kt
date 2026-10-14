package com.acme.transport.crewing.roster.threshold

/** A rule a roster threshold must pass. Check returns its problems, or none. */
interface RosterThresholdRule {
    fun check(item: RosterThreshold): MutableList<String>
}

data class RosterThresholdCreatedAtRangeRule(var min: Int, var max: Int) : RosterThresholdRule {
    override fun check(item: RosterThreshold): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterThresholdRequiredShiftCodeRule(var message: String) : RosterThresholdRule {
    override fun check(item: RosterThreshold): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterThresholdRequiredCodeRule(var message: String) : RosterThresholdRule {
    override fun check(item: RosterThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterThresholdRequiredTitleRule(var message: String) : RosterThresholdRule {
    override fun check(item: RosterThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterThresholdCodePrefixRule(var prefix: String) : RosterThresholdRule {
    override fun check(item: RosterThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterThresholdRequiredDriverIdRule(var message: String) : RosterThresholdRule {
    override fun check(item: RosterThreshold): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster threshold. */
data class RosterThresholdValidator(var rules: MutableList<RosterThresholdRule>) {
    fun validate(item: RosterThreshold): MutableList<String> {
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

    fun requireValid(item: RosterThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterThresholdValidator(): RosterThresholdValidator {
    return RosterThresholdValidator(
        mutableListOf<RosterThresholdRule>(
            RosterThresholdCreatedAtRangeRule(16, 32),
            RosterThresholdRequiredShiftCodeRule("roster threshold needs a value"),
            RosterThresholdRequiredCodeRule("roster threshold needs a value"),
            RosterThresholdRequiredTitleRule("roster threshold needs this field"),
            RosterThresholdCodePrefixRule("X"),
            RosterThresholdRequiredDriverIdRule("roster threshold needs an entry"),
        ),
    )
}
