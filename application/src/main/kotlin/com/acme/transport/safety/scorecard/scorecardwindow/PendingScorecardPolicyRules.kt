package com.acme.transport.safety.scorecard.scorecardwindow

/** A rule a pending scorecard policy must pass. Check returns its problems, or none. */
interface PendingScorecardPolicyRule {
    fun check(item: PendingScorecardPolicy): MutableList<String>
}

data class PendingScorecardPolicyDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PendingScorecardPolicyRule {
    override fun check(item: PendingScorecardPolicy): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardPolicyChannelLengthRule(var maxLength: Int) : PendingScorecardPolicyRule {
    override fun check(item: PendingScorecardPolicy): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardPolicyCodePrefixRule(var prefix: String) : PendingScorecardPolicyRule {
    override fun check(item: PendingScorecardPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending scorecard policy. */
data class PendingScorecardPolicyValidator(var rules: MutableList<PendingScorecardPolicyRule>) {
    fun validate(item: PendingScorecardPolicy): MutableList<String> {
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

    fun requireValid(item: PendingScorecardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingScorecardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingScorecardPolicyValidator(): PendingScorecardPolicyValidator {
    return PendingScorecardPolicyValidator(
        mutableListOf<PendingScorecardPolicyRule>(
            PendingScorecardPolicyDurationMinutesRangeRule(80, 255),
            PendingScorecardPolicyChannelLengthRule(32),
            PendingScorecardPolicyCodePrefixRule("OPS"),
        ),
    )
}
