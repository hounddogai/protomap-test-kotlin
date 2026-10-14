package com.acme.transport.network.transittime.forecast

/** A rule a manual transit time batch must pass. Check returns its problems, or none. */
interface ManualTransitTimeBatchRule {
    fun check(item: ManualTransitTimeBatch): MutableList<String>
}

data class ManualTransitTimeBatchRequiredTitleRule(var message: String) : ManualTransitTimeBatchRule {
    override fun check(item: ManualTransitTimeBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTransitTimeBatchTimezoneLengthRule(var maxLength: Int) : ManualTransitTimeBatchRule {
    override fun check(item: ManualTransitTimeBatch): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTransitTimeBatchRequiredTimezoneRule(var message: String) : ManualTransitTimeBatchRule {
    override fun check(item: ManualTransitTimeBatch): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTransitTimeBatchMaximumRangeRule(
    var min: Double,
    var max: Double,
) : ManualTransitTimeBatchRule {
    override fun check(item: ManualTransitTimeBatch): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTransitTimeBatchCategoryLengthRule(var maxLength: Int) : ManualTransitTimeBatchRule {
    override fun check(item: ManualTransitTimeBatch): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual transit time batch. */
data class ManualTransitTimeBatchValidator(var rules: MutableList<ManualTransitTimeBatchRule>) {
    fun validate(item: ManualTransitTimeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTransitTimeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTransitTimeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTransitTimeBatchValidator(): ManualTransitTimeBatchValidator {
    return ManualTransitTimeBatchValidator(
        mutableListOf<ManualTransitTimeBatchRule>(
            ManualTransitTimeBatchRequiredTitleRule("manual transit time batch needs this field"),
            ManualTransitTimeBatchTimezoneLengthRule(16),
            ManualTransitTimeBatchRequiredTimezoneRule("manual transit time batch needs an entry"),
            ManualTransitTimeBatchMaximumRangeRule(6.6, 4810.9),
            ManualTransitTimeBatchCategoryLengthRule(80),
        ),
    )
}
