package com.acme.transport.safety.scorecard.scorecardthreshold

/** A rule a rolling scorecard threshold must pass. Check returns its problems, or none. */
interface RollingScorecardThresholdRule {
    fun check(item: RollingScorecardThreshold): MutableList<String>
}

data class RollingScorecardThresholdQuantityRangeRule(
    var min: Int,
    var max: Int,
) : RollingScorecardThresholdRule {
    override fun check(item: RollingScorecardThreshold): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardThresholdLabelLengthRule(var maxLength: Int) : RollingScorecardThresholdRule {
    override fun check(item: RollingScorecardThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardThresholdLaneLengthRule(var maxLength: Int) : RollingScorecardThresholdRule {
    override fun check(item: RollingScorecardThreshold): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardThresholdCodePrefixRule(var prefix: String) : RollingScorecardThresholdRule {
    override fun check(item: RollingScorecardThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardThresholdCodeLengthRule(var maxLength: Int) : RollingScorecardThresholdRule {
    override fun check(item: RollingScorecardThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling scorecard threshold. */
data class RollingScorecardThresholdValidator(var rules: MutableList<RollingScorecardThresholdRule>) {
    fun validate(item: RollingScorecardThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingScorecardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingScorecardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingScorecardThresholdValidator(): RollingScorecardThresholdValidator {
    return RollingScorecardThresholdValidator(
        mutableListOf<RollingScorecardThresholdRule>(
            RollingScorecardThresholdQuantityRangeRule(255, 255),
            RollingScorecardThresholdLabelLengthRule(1000),
            RollingScorecardThresholdLaneLengthRule(3600),
            RollingScorecardThresholdCodePrefixRule("ACM"),
            RollingScorecardThresholdCodeLengthRule(86400),
        ),
    )
}
