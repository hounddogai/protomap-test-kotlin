package com.acme.transport.terminal.dwell.preference

/** A rule a manual dwell estimate must pass. Check returns its problems, or none. */
interface ManualDwellEstimateRule {
    fun check(item: ManualDwellEstimate): MutableList<String>
}

data class ManualDwellEstimateNameLengthRule(var maxLength: Int) : ManualDwellEstimateRule {
    override fun check(item: ManualDwellEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellEstimateCapacityRangeRule(var min: Int, var max: Int) : ManualDwellEstimateRule {
    override fun check(item: ManualDwellEstimate): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellEstimateScoreRangeRule(var min: Double, var max: Double) : ManualDwellEstimateRule {
    override fun check(item: ManualDwellEstimate): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dwell estimate. */
data class ManualDwellEstimateValidator(var rules: MutableList<ManualDwellEstimateRule>) {
    fun validate(item: ManualDwellEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDwellEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDwellEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDwellEstimateValidator(): ManualDwellEstimateValidator {
    return ManualDwellEstimateValidator(
        mutableListOf<ManualDwellEstimateRule>(
            ManualDwellEstimateNameLengthRule(64),
            ManualDwellEstimateCapacityRangeRule(120, 255),
            ManualDwellEstimateScoreRangeRule(8.2, 3850.8),
        ),
    )
}
