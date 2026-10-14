package com.acme.transport.terminal.sortation.group

/** A rule a capped sortation estimate must pass. Check returns its problems, or none. */
interface CappedSortationEstimateRule {
    fun check(item: CappedSortationEstimate): MutableList<String>
}

data class CappedSortationEstimateEndsAtRangeRule(var min: Int, var max: Int) : CappedSortationEstimateRule {
    override fun check(item: CappedSortationEstimate): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedSortationEstimateCodePrefixRule(var prefix: String) : CappedSortationEstimateRule {
    override fun check(item: CappedSortationEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedSortationEstimateRequiredCodeRule(var message: String) : CappedSortationEstimateRule {
    override fun check(item: CappedSortationEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedSortationEstimateRequiredLabelRule(var message: String) : CappedSortationEstimateRule {
    override fun check(item: CappedSortationEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped sortation estimate. */
data class CappedSortationEstimateValidator(var rules: MutableList<CappedSortationEstimateRule>) {
    fun validate(item: CappedSortationEstimate): MutableList<String> {
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

    fun requireValid(item: CappedSortationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedSortationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedSortationEstimateValidator(): CappedSortationEstimateValidator {
    return CappedSortationEstimateValidator(
        mutableListOf<CappedSortationEstimateRule>(
            CappedSortationEstimateEndsAtRangeRule(80, 1000),
            CappedSortationEstimateCodePrefixRule("Q"),
            CappedSortationEstimateRequiredCodeRule("capped sortation estimate needs a value"),
            CappedSortationEstimateRequiredLabelRule("capped sortation estimate needs this field"),
        ),
    )
}
