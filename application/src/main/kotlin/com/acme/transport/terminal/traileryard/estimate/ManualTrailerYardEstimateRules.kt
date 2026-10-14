package com.acme.transport.terminal.traileryard.estimate

/** A rule a manual trailer yard estimate must pass. Check returns its problems, or none. */
interface ManualTrailerYardEstimateRule {
    fun check(item: ManualTrailerYardEstimate): MutableList<String>
}

data class ManualTrailerYardEstimateRequiredLocaleRule(var message: String) : ManualTrailerYardEstimateRule {
    override fun check(item: ManualTrailerYardEstimate): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardEstimateRequiredTitleRule(var message: String) : ManualTrailerYardEstimateRule {
    override fun check(item: ManualTrailerYardEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardEstimateCapacityRangeRule(
    var min: Int,
    var max: Int,
) : ManualTrailerYardEstimateRule {
    override fun check(item: ManualTrailerYardEstimate): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardEstimateCodeLengthRule(var maxLength: Int) : ManualTrailerYardEstimateRule {
    override fun check(item: ManualTrailerYardEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerYardEstimateLocaleLengthRule(var maxLength: Int) : ManualTrailerYardEstimateRule {
    override fun check(item: ManualTrailerYardEstimate): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer yard estimate. */
data class ManualTrailerYardEstimateValidator(var rules: MutableList<ManualTrailerYardEstimateRule>) {
    fun validate(item: ManualTrailerYardEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTrailerYardEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerYardEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerYardEstimateValidator(): ManualTrailerYardEstimateValidator {
    return ManualTrailerYardEstimateValidator(
        mutableListOf<ManualTrailerYardEstimateRule>(
            ManualTrailerYardEstimateRequiredLocaleRule("manual trailer yard estimate needs a value"),
            ManualTrailerYardEstimateRequiredTitleRule("manual trailer yard estimate needs this field"),
            ManualTrailerYardEstimateCapacityRangeRule(1000, 86400),
            ManualTrailerYardEstimateCodeLengthRule(64),
            ManualTrailerYardEstimateLocaleLengthRule(255),
        ),
    )
}
