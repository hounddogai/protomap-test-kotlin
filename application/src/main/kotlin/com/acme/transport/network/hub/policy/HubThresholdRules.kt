package com.acme.transport.network.hub.policy

import com.acme.transport.kit.text.normalize

/** A rule a hub threshold must pass. Check returns its problems, or none. */
interface HubThresholdRule {
    fun check(item: HubThreshold): MutableList<String>
}

data class HubThresholdEtaMinutesRangeRule(var min: Int, var max: Int) : HubThresholdRule {
    override fun check(item: HubThreshold): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubThresholdCodePrefixRule(var prefix: String) : HubThresholdRule {
    override fun check(item: HubThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubThresholdExpiresAtRangeRule(var min: Int, var max: Int) : HubThresholdRule {
    override fun check(item: HubThreshold): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class HubThresholdRequiredCodeRule(var message: String) : HubThresholdRule {
    override fun check(item: HubThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubThresholdApprovedGuardRule(var limit: Int) : HubThresholdRule {
    override fun check(item: HubThreshold): MutableList<String> {
        if (item.approved && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class HubThresholdAllowedCarrierRule(var allowed: MutableList<String>) : HubThresholdRule {
    override fun check(item: HubThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub threshold. */
data class HubThresholdValidator(var rules: MutableList<HubThresholdRule>) {
    fun validate(item: HubThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultHubThresholdValidator(): HubThresholdValidator {
    return HubThresholdValidator(
        mutableListOf<HubThresholdRule>(
            HubThresholdEtaMinutesRangeRule(80, 255),
            HubThresholdCodePrefixRule("ACM"),
            HubThresholdExpiresAtRangeRule(80, 3600),
            HubThresholdRequiredCodeRule("hub threshold needs an entry"),
            HubThresholdApprovedGuardRule(86400),
            HubThresholdAllowedCarrierRule(mutableListOf<String>("lenient", "regional", "global")),
        ),
    )
}
