package com.acme.transport.terminal.sortation.variance

/** A rule a sortation variance must pass. Check returns its problems, or none. */
interface SortationVarianceRule {
    fun check(item: SortationVariance): MutableList<String>
}

data class SortationVarianceRequiredNameRule(var message: String) : SortationVarianceRule {
    override fun check(item: SortationVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationVarianceRatioRangeRule(var min: Double, var max: Double) : SortationVarianceRule {
    override fun check(item: SortationVariance): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationVarianceDescriptionLengthRule(var maxLength: Int) : SortationVarianceRule {
    override fun check(item: SortationVariance): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationVarianceVersionRangeRule(var min: Int, var max: Int) : SortationVarianceRule {
    override fun check(item: SortationVariance): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class SortationVarianceBatchSizeRangeRule(var min: Int, var max: Int) : SortationVarianceRule {
    override fun check(item: SortationVariance): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation variance. */
data class SortationVarianceValidator(var rules: MutableList<SortationVarianceRule>) {
    fun validate(item: SortationVariance): MutableList<String> {
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

    fun requireValid(item: SortationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationVarianceValidator(): SortationVarianceValidator {
    return SortationVarianceValidator(
        mutableListOf<SortationVarianceRule>(
            SortationVarianceRequiredNameRule("sortation variance needs a value"),
            SortationVarianceRatioRangeRule(0.9, 2401.6),
            SortationVarianceDescriptionLengthRule(120),
            SortationVarianceVersionRangeRule(120, 255),
            SortationVarianceBatchSizeRangeRule(16, 86400),
        ),
    )
}
