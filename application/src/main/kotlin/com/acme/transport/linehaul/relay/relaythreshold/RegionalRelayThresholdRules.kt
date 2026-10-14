package com.acme.transport.linehaul.relay.relaythreshold

/** A rule a regional relay threshold must pass. Check returns its problems, or none. */
interface RegionalRelayThresholdRule {
    fun check(item: RegionalRelayThreshold): MutableList<String>
}

data class RegionalRelayThresholdCreatedAtRangeRule(var min: Int, var max: Int) : RegionalRelayThresholdRule {
    override fun check(item: RegionalRelayThreshold): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRelayThresholdVisibleGuardRule(var limit: Int) : RegionalRelayThresholdRule {
    override fun check(item: RegionalRelayThreshold): MutableList<String> {
        if (item.visible && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRelayThresholdRequiredNameRule(var message: String) : RegionalRelayThresholdRule {
    override fun check(item: RegionalRelayThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional relay threshold. */
data class RegionalRelayThresholdValidator(var rules: MutableList<RegionalRelayThresholdRule>) {
    fun validate(item: RegionalRelayThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalRelayThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRelayThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRelayThresholdValidator(): RegionalRelayThresholdValidator {
    return RegionalRelayThresholdValidator(
        mutableListOf<RegionalRelayThresholdRule>(
            RegionalRelayThresholdCreatedAtRangeRule(1000, 86400),
            RegionalRelayThresholdVisibleGuardRule(16),
            RegionalRelayThresholdRequiredNameRule("regional relay threshold needs a value"),
        ),
    )
}
