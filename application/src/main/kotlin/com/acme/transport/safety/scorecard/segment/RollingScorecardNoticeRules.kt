package com.acme.transport.safety.scorecard.segment

/** A rule a rolling scorecard notice must pass. Check returns its problems, or none. */
interface RollingScorecardNoticeRule {
    fun check(item: RollingScorecardNotice): MutableList<String>
}

data class RollingScorecardNoticeWeightRangeRule(
    var min: Double,
    var max: Double,
) : RollingScorecardNoticeRule {
    override fun check(item: RollingScorecardNotice): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class RollingScorecardNoticeTitleLengthRule(var maxLength: Int) : RollingScorecardNoticeRule {
    override fun check(item: RollingScorecardNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingScorecardNoticePalletsRangeRule(var min: Int, var max: Int) : RollingScorecardNoticeRule {
    override fun check(item: RollingScorecardNotice): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RollingScorecardNoticeUpdatedAtRangeRule(var min: Int, var max: Int) : RollingScorecardNoticeRule {
    override fun check(item: RollingScorecardNotice): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling scorecard notice. */
data class RollingScorecardNoticeValidator(var rules: MutableList<RollingScorecardNoticeRule>) {
    fun validate(item: RollingScorecardNotice): MutableList<String> {
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

    fun requireValid(item: RollingScorecardNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingScorecardNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingScorecardNoticeValidator(): RollingScorecardNoticeValidator {
    return RollingScorecardNoticeValidator(
        mutableListOf<RollingScorecardNoticeRule>(
            RollingScorecardNoticeWeightRangeRule(2.6, 3077.6),
            RollingScorecardNoticeTitleLengthRule(120),
            RollingScorecardNoticePalletsRangeRule(16, 32),
            RollingScorecardNoticeUpdatedAtRangeRule(32, 64),
        ),
    )
}
