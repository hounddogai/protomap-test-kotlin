package com.acme.transport.network.hub.metric

/** A rule a manual hub baseline must pass. Check returns its problems, or none. */
interface ManualHubBaselineRule {
    fun check(item: ManualHubBaseline): MutableList<String>
}

data class ManualHubBaselinePriorityRangeRule(var min: Int, var max: Int) : ManualHubBaselineRule {
    override fun check(item: ManualHubBaseline): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubBaselineTitleLengthRule(var maxLength: Int) : ManualHubBaselineRule {
    override fun check(item: ManualHubBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubBaselineApprovedGuardRule(var limit: Int) : ManualHubBaselineRule {
    override fun check(item: ManualHubBaseline): MutableList<String> {
        if (item.approved && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubBaselineDriverIdLengthRule(var maxLength: Int) : ManualHubBaselineRule {
    override fun check(item: ManualHubBaseline): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHubBaselineSequenceRangeRule(var min: Int, var max: Int) : ManualHubBaselineRule {
    override fun check(item: ManualHubBaseline): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual hub baseline. */
data class ManualHubBaselineValidator(var rules: MutableList<ManualHubBaselineRule>) {
    fun validate(item: ManualHubBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualHubBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualHubBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultManualHubBaselineValidator(): ManualHubBaselineValidator {
    return ManualHubBaselineValidator(
        mutableListOf<ManualHubBaselineRule>(
            ManualHubBaselinePriorityRangeRule(255, 3600),
            ManualHubBaselineTitleLengthRule(86400),
            ManualHubBaselineApprovedGuardRule(64),
            ManualHubBaselineDriverIdLengthRule(16),
            ManualHubBaselineSequenceRangeRule(16, 3600),
        ),
    )
}
