package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.text.normalize

/** A rule a rest break tier must pass. Check returns its problems, or none. */
interface RestBreakTierRule {
    fun check(item: RestBreakTier): MutableList<String>
}

data class RestBreakTierAllowedChannelRule(var allowed: MutableList<String>) : RestBreakTierRule {
    override fun check(item: RestBreakTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTierChannelLengthRule(var maxLength: Int) : RestBreakTierRule {
    override fun check(item: RestBreakTier): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTierNameLengthRule(var maxLength: Int) : RestBreakTierRule {
    override fun check(item: RestBreakTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTierRequiredChannelRule(var message: String) : RestBreakTierRule {
    override fun check(item: RestBreakTier): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTierVehicleIdLengthRule(var maxLength: Int) : RestBreakTierRule {
    override fun check(item: RestBreakTier): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break tier. */
data class RestBreakTierValidator(var rules: MutableList<RestBreakTierRule>) {
    fun validate(item: RestBreakTier): MutableList<String> {
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

    fun requireValid(item: RestBreakTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakTierValidator(): RestBreakTierValidator {
    return RestBreakTierValidator(
        mutableListOf<RestBreakTierRule>(
            RestBreakTierAllowedChannelRule(mutableListOf<String>("capped", "capped", "global")),
            RestBreakTierChannelLengthRule(32),
            RestBreakTierNameLengthRule(1000),
            RestBreakTierRequiredChannelRule("rest break tier needs this field"),
            RestBreakTierVehicleIdLengthRule(64),
        ),
    )
}
