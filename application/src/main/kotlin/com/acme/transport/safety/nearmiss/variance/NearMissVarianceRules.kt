package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.normalize

/** A rule a near miss variance must pass. Check returns its problems, or none. */
interface NearMissVarianceRule {
    fun check(item: NearMissVariance): MutableList<String>
}

data class NearMissVarianceAllowedChannelRule(var allowed: MutableList<String>) : NearMissVarianceRule {
    override fun check(item: NearMissVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class NearMissVarianceRequiredCodeRule(var message: String) : NearMissVarianceRule {
    override fun check(item: NearMissVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissVarianceRequiredChannelRule(var message: String) : NearMissVarianceRule {
    override fun check(item: NearMissVariance): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissVarianceScoreRangeRule(var min: Double, var max: Double) : NearMissVarianceRule {
    override fun check(item: NearMissVariance): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissVarianceRequiredSegmentRule(var message: String) : NearMissVarianceRule {
    override fun check(item: NearMissVariance): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissVarianceCodePrefixRule(var prefix: String) : NearMissVarianceRule {
    override fun check(item: NearMissVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss variance. */
data class NearMissVarianceValidator(var rules: MutableList<NearMissVarianceRule>) {
    fun validate(item: NearMissVariance): MutableList<String> {
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

    fun requireValid(item: NearMissVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissVarianceValidator(): NearMissVarianceValidator {
    return NearMissVarianceValidator(
        mutableListOf<NearMissVarianceRule>(
            NearMissVarianceAllowedChannelRule(mutableListOf<String>("capped")),
            NearMissVarianceRequiredCodeRule("near miss variance needs this field"),
            NearMissVarianceRequiredChannelRule("near miss variance needs an entry"),
            NearMissVarianceScoreRangeRule(5.8, 1343.1),
            NearMissVarianceRequiredSegmentRule("near miss variance needs an entry"),
            NearMissVarianceCodePrefixRule("REF"),
        ),
    )
}
