package com.acme.transport.customs.clearance.overrideitem

/** A rule a clearance score must pass. Check returns its problems, or none. */
interface ClearanceScoreRule {
    fun check(item: ClearanceScore): MutableList<String>
}

data class ClearanceScoreSegmentLengthRule(var maxLength: Int) : ClearanceScoreRule {
    override fun check(item: ClearanceScore): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceScoreRetriesRangeRule(var min: Int, var max: Int) : ClearanceScoreRule {
    override fun check(item: ClearanceScore): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class ClearanceScoreChannelLengthRule(var maxLength: Int) : ClearanceScoreRule {
    override fun check(item: ClearanceScore): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceScoreScoreRangeRule(var min: Double, var max: Double) : ClearanceScoreRule {
    override fun check(item: ClearanceScore): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceScoreRequiredCodeRule(var message: String) : ClearanceScoreRule {
    override fun check(item: ClearanceScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceScoreRequiredSegmentRule(var message: String) : ClearanceScoreRule {
    override fun check(item: ClearanceScore): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance score. */
data class ClearanceScoreValidator(var rules: MutableList<ClearanceScoreRule>) {
    fun validate(item: ClearanceScore): MutableList<String> {
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

    fun requireValid(item: ClearanceScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceScore>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceScoreValidator(): ClearanceScoreValidator {
    return ClearanceScoreValidator(
        mutableListOf<ClearanceScoreRule>(
            ClearanceScoreSegmentLengthRule(80),
            ClearanceScoreRetriesRangeRule(255, 3600),
            ClearanceScoreChannelLengthRule(86400),
            ClearanceScoreScoreRangeRule(2.5, 2957.8),
            ClearanceScoreRequiredCodeRule("clearance score needs a value"),
            ClearanceScoreRequiredSegmentRule("clearance score needs a value"),
        ),
    )
}
