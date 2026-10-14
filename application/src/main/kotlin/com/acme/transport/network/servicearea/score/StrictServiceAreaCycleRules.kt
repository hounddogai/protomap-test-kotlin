package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.text.normalize

/** A rule a strict service area cycle must pass. Check returns its problems, or none. */
interface StrictServiceAreaCycleRule {
    fun check(item: StrictServiceAreaCycle): MutableList<String>
}

data class StrictServiceAreaCycleAllowedChannelRule(
    var allowed: MutableList<String>,
) : StrictServiceAreaCycleRule {
    override fun check(item: StrictServiceAreaCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaCycleRequiredCodeRule(var message: String) : StrictServiceAreaCycleRule {
    override fun check(item: StrictServiceAreaCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaCycleChannelLengthRule(var maxLength: Int) : StrictServiceAreaCycleRule {
    override fun check(item: StrictServiceAreaCycle): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaCycleRequiredChannelRule(var message: String) : StrictServiceAreaCycleRule {
    override fun check(item: StrictServiceAreaCycle): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict service area cycle. */
data class StrictServiceAreaCycleValidator(var rules: MutableList<StrictServiceAreaCycleRule>) {
    fun validate(item: StrictServiceAreaCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictServiceAreaCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServiceAreaCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServiceAreaCycleValidator(): StrictServiceAreaCycleValidator {
    return StrictServiceAreaCycleValidator(
        mutableListOf<StrictServiceAreaCycleRule>(
            StrictServiceAreaCycleAllowedChannelRule(mutableListOf<String>("default")),
            StrictServiceAreaCycleRequiredCodeRule("strict service area cycle needs an entry"),
            StrictServiceAreaCycleChannelLengthRule(64),
            StrictServiceAreaCycleRequiredChannelRule("strict service area cycle needs a value"),
        ),
    )
}
