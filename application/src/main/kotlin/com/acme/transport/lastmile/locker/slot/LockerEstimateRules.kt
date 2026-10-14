package com.acme.transport.lastmile.locker.slot

/** A rule a locker estimate must pass. Check returns its problems, or none. */
interface LockerEstimateRule {
    fun check(item: LockerEstimate): MutableList<String>
}

data class LockerEstimateRequiredLabelRule(var message: String) : LockerEstimateRule {
    override fun check(item: LockerEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerEstimateBatchSizeRangeRule(var min: Int, var max: Int) : LockerEstimateRule {
    override fun check(item: LockerEstimate): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class LockerEstimateCodePrefixRule(var prefix: String) : LockerEstimateRule {
    override fun check(item: LockerEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerEstimateBudgetRangeRule(var min: Double, var max: Double) : LockerEstimateRule {
    override fun check(item: LockerEstimate): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerEstimateWindowDaysRangeRule(var min: Int, var max: Int) : LockerEstimateRule {
    override fun check(item: LockerEstimate): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker estimate. */
data class LockerEstimateValidator(var rules: MutableList<LockerEstimateRule>) {
    fun validate(item: LockerEstimate): MutableList<String> {
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

    fun requireValid(item: LockerEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerEstimateValidator(): LockerEstimateValidator {
    return LockerEstimateValidator(
        mutableListOf<LockerEstimateRule>(
            LockerEstimateRequiredLabelRule("locker estimate needs this field"),
            LockerEstimateBatchSizeRangeRule(32, 3600),
            LockerEstimateCodePrefixRule("ACM"),
            LockerEstimateBudgetRangeRule(8.4, 618.3),
            LockerEstimateWindowDaysRangeRule(64, 80),
        ),
    )
}
