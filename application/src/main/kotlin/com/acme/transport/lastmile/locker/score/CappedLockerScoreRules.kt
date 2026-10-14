package com.acme.transport.lastmile.locker.score

/** A rule a capped locker score must pass. Check returns its problems, or none. */
interface CappedLockerScoreRule {
    fun check(item: CappedLockerScore): MutableList<String>
}

data class CappedLockerScoreLabelLengthRule(var maxLength: Int) : CappedLockerScoreRule {
    override fun check(item: CappedLockerScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerScoreRequiredLabelRule(var message: String) : CappedLockerScoreRule {
    override fun check(item: CappedLockerScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerScoreGraceDaysRangeRule(var min: Int, var max: Int) : CappedLockerScoreRule {
    override fun check(item: CappedLockerScore): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerScoreCodeLengthRule(var maxLength: Int) : CappedLockerScoreRule {
    override fun check(item: CappedLockerScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped locker score. */
data class CappedLockerScoreValidator(var rules: MutableList<CappedLockerScoreRule>) {
    fun validate(item: CappedLockerScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedLockerScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLockerScore>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLockerScoreValidator(): CappedLockerScoreValidator {
    return CappedLockerScoreValidator(
        mutableListOf<CappedLockerScoreRule>(
            CappedLockerScoreLabelLengthRule(32),
            CappedLockerScoreRequiredLabelRule("capped locker score needs an entry"),
            CappedLockerScoreGraceDaysRangeRule(64, 3600),
            CappedLockerScoreCodeLengthRule(3600),
        ),
    )
}
