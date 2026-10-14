package com.acme.transport.customs.hold.adjustment

/** A rule a hold score must pass. Check returns its problems, or none. */
interface HoldScoreRule {
    fun check(item: HoldScore): MutableList<String>
}

data class HoldScorePinnedGuardRule(var limit: Int) : HoldScoreRule {
    override fun check(item: HoldScore): MutableList<String> {
        if (item.pinned && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class HoldScoreAttemptsRangeRule(var min: Int, var max: Int) : HoldScoreRule {
    override fun check(item: HoldScore): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class HoldScoreCodeLengthRule(var maxLength: Int) : HoldScoreRule {
    override fun check(item: HoldScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldScoreRequiredNameRule(var message: String) : HoldScoreRule {
    override fun check(item: HoldScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldScoreRequiredCodeRule(var message: String) : HoldScoreRule {
    override fun check(item: HoldScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold score. */
data class HoldScoreValidator(var rules: MutableList<HoldScoreRule>) {
    fun validate(item: HoldScore): MutableList<String> {
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

    fun requireValid(item: HoldScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldScore>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldScoreValidator(): HoldScoreValidator {
    return HoldScoreValidator(
        mutableListOf<HoldScoreRule>(
            HoldScorePinnedGuardRule(86400),
            HoldScoreAttemptsRangeRule(255, 86400),
            HoldScoreCodeLengthRule(3600),
            HoldScoreRequiredNameRule("hold score needs this field"),
            HoldScoreRequiredCodeRule("hold score needs this field"),
        ),
    )
}
