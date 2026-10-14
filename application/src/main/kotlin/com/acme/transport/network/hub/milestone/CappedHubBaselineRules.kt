package com.acme.transport.network.hub.milestone

/** A rule a capped hub baseline must pass. Check returns its problems, or none. */
interface CappedHubBaselineRule {
    fun check(item: CappedHubBaseline): MutableList<String>
}

data class CappedHubBaselineBatchSizeRangeRule(var min: Int, var max: Int) : CappedHubBaselineRule {
    override fun check(item: CappedHubBaseline): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubBaselineCodeLengthRule(var maxLength: Int) : CappedHubBaselineRule {
    override fun check(item: CappedHubBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHubBaselineRequiredCodeRule(var message: String) : CappedHubBaselineRule {
    override fun check(item: CappedHubBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedHubBaselineDurationMinutesRangeRule(var min: Int, var max: Int) : CappedHubBaselineRule {
    override fun check(item: CappedHubBaseline): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped hub baseline. */
data class CappedHubBaselineValidator(var rules: MutableList<CappedHubBaselineRule>) {
    fun validate(item: CappedHubBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedHubBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedHubBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedHubBaselineValidator(): CappedHubBaselineValidator {
    return CappedHubBaselineValidator(
        mutableListOf<CappedHubBaselineRule>(
            CappedHubBaselineBatchSizeRangeRule(16, 86400),
            CappedHubBaselineCodeLengthRule(3600),
            CappedHubBaselineRequiredCodeRule("capped hub baseline needs an entry"),
            CappedHubBaselineDurationMinutesRangeRule(1000, 86400),
        ),
    )
}
