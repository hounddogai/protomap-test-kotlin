package com.acme.transport.safety.scorecard.scorecardprofile

/** A rule a rolling scorecard profile must pass. Check returns its problems, or none. */
interface RollingScorecardProfileRule {
    fun check(item: RollingScorecardProfile): MutableList<String>
}

data class RollingScorecardProfileRequiredCodeRule(var message: String) : RollingScorecardProfileRule {
    override fun check(item: RollingScorecardProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardProfileCodePrefixRule(var prefix: String) : RollingScorecardProfileRule {
    override fun check(item: RollingScorecardProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardProfileStopCountRangeRule(var min: Int, var max: Int) : RollingScorecardProfileRule {
    override fun check(item: RollingScorecardProfile): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class RollingScorecardProfileLabelLengthRule(var maxLength: Int) : RollingScorecardProfileRule {
    override fun check(item: RollingScorecardProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardProfileRequiredShiftCodeRule(var message: String) : RollingScorecardProfileRule {
    override fun check(item: RollingScorecardProfile): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling scorecard profile. */
data class RollingScorecardProfileValidator(var rules: MutableList<RollingScorecardProfileRule>) {
    fun validate(item: RollingScorecardProfile): MutableList<String> {
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

    fun requireValid(item: RollingScorecardProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingScorecardProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingScorecardProfileValidator(): RollingScorecardProfileValidator {
    return RollingScorecardProfileValidator(
        mutableListOf<RollingScorecardProfileRule>(
            RollingScorecardProfileRequiredCodeRule("rolling scorecard profile needs this field"),
            RollingScorecardProfileCodePrefixRule("REF"),
            RollingScorecardProfileStopCountRangeRule(32, 64),
            RollingScorecardProfileLabelLengthRule(255),
            RollingScorecardProfileRequiredShiftCodeRule("rolling scorecard profile needs this field"),
        ),
    )
}
