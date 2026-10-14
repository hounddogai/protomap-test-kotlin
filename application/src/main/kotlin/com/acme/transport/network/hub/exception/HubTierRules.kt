package com.acme.transport.network.hub.exception

/** A rule a hub tier must pass. Check returns its problems, or none. */
interface HubTierRule {
    fun check(item: HubTier): MutableList<String>
}

data class HubTierRequiredTitleRule(var message: String) : HubTierRule {
    override fun check(item: HubTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubTierRequiredLaneRule(var message: String) : HubTierRule {
    override fun check(item: HubTier): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubTierTitleLengthRule(var maxLength: Int) : HubTierRule {
    override fun check(item: HubTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubTierOdometerRangeRule(var min: Int, var max: Int) : HubTierRule {
    override fun check(item: HubTier): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class HubTierCodePrefixRule(var prefix: String) : HubTierRule {
    override fun check(item: HubTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubTierRequiredCodeRule(var message: String) : HubTierRule {
    override fun check(item: HubTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub tier. */
data class HubTierValidator(var rules: MutableList<HubTierRule>) {
    fun validate(item: HubTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubTier>): MutableMap<String, MutableList<String>> {
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

fun defaultHubTierValidator(): HubTierValidator {
    return HubTierValidator(
        mutableListOf<HubTierRule>(
            HubTierRequiredTitleRule("hub tier needs an entry"),
            HubTierRequiredLaneRule("hub tier needs an entry"),
            HubTierTitleLengthRule(120),
            HubTierOdometerRangeRule(16, 120),
            HubTierCodePrefixRule("ACM"),
            HubTierRequiredCodeRule("hub tier needs an entry"),
        ),
    )
}
