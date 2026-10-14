package com.acme.transport.network.hub.hubentry

/** A rule a hub bundle must pass. Check returns its problems, or none. */
interface HubBundleRule {
    fun check(item: HubBundle): MutableList<String>
}

data class HubBundleCodePrefixRule(var prefix: String) : HubBundleRule {
    override fun check(item: HubBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubBundleCodeLengthRule(var maxLength: Int) : HubBundleRule {
    override fun check(item: HubBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubBundleRequiredChannelRule(var message: String) : HubBundleRule {
    override fun check(item: HubBundle): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubBundleRequiredLabelRule(var message: String) : HubBundleRule {
    override fun check(item: HubBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubBundleAmountRangeRule(var min: Double, var max: Double) : HubBundleRule {
    override fun check(item: HubBundle): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a hub bundle. */
data class HubBundleValidator(var rules: MutableList<HubBundleRule>) {
    fun validate(item: HubBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultHubBundleValidator(): HubBundleValidator {
    return HubBundleValidator(
        mutableListOf<HubBundleRule>(
            HubBundleCodePrefixRule("ACM"),
            HubBundleCodeLengthRule(80),
            HubBundleRequiredChannelRule("hub bundle needs an entry"),
            HubBundleRequiredLabelRule("hub bundle needs this field"),
            HubBundleAmountRangeRule(0.7, 2761.6),
        ),
    )
}
