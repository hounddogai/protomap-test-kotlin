package com.acme.transport.customs.hold.baseline

import com.acme.transport.kit.text.normalize

/** A rule a hold baseline must pass. Check returns its problems, or none. */
interface HoldBaselineRule {
    fun check(item: HoldBaseline): MutableList<String>
}

data class HoldBaselineTitleLengthRule(var maxLength: Int) : HoldBaselineRule {
    override fun check(item: HoldBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldBaselineRequiredCodeRule(var message: String) : HoldBaselineRule {
    override fun check(item: HoldBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldBaselineAllowedChannelRule(var allowed: MutableList<String>) : HoldBaselineRule {
    override fun check(item: HoldBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is expired")
        }
        return mutableListOf<String>()
    }
}

data class HoldBaselineChannelLengthRule(var maxLength: Int) : HoldBaselineRule {
    override fun check(item: HoldBaseline): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold baseline. */
data class HoldBaselineValidator(var rules: MutableList<HoldBaselineRule>) {
    fun validate(item: HoldBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldBaselineValidator(): HoldBaselineValidator {
    return HoldBaselineValidator(
        mutableListOf<HoldBaselineRule>(
            HoldBaselineTitleLengthRule(16),
            HoldBaselineRequiredCodeRule("hold baseline needs an entry"),
            HoldBaselineAllowedChannelRule(mutableListOf<String>("manual")),
            HoldBaselineChannelLengthRule(80),
        ),
    )
}
