package com.acme.transport.fleetops.utilization.utilizationsegment

/** A rule a fallback utilization summary must pass. Check returns its problems, or none. */
interface FallbackUtilizationSummaryRule {
    fun check(item: FallbackUtilizationSummary): MutableList<String>
}

data class FallbackUtilizationSummaryRequiredCarrierRule(var message: String) : FallbackUtilizationSummaryRule {
    override fun check(item: FallbackUtilizationSummary): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationSummaryCodeLengthRule(var maxLength: Int) : FallbackUtilizationSummaryRule {
    override fun check(item: FallbackUtilizationSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationSummaryScoreRangeRule(
    var min: Double,
    var max: Double,
) : FallbackUtilizationSummaryRule {
    override fun check(item: FallbackUtilizationSummary): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationSummaryRequiredChecksumRule(
    var message: String,
) : FallbackUtilizationSummaryRule {
    override fun check(item: FallbackUtilizationSummary): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback utilization summary. */
data class FallbackUtilizationSummaryValidator(var rules: MutableList<FallbackUtilizationSummaryRule>) {
    fun validate(item: FallbackUtilizationSummary): MutableList<String> {
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

    fun requireValid(item: FallbackUtilizationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackUtilizationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackUtilizationSummaryValidator(): FallbackUtilizationSummaryValidator {
    return FallbackUtilizationSummaryValidator(
        mutableListOf<FallbackUtilizationSummaryRule>(
            FallbackUtilizationSummaryRequiredCarrierRule("fallback utilization summary needs an entry"),
            FallbackUtilizationSummaryCodeLengthRule(1000),
            FallbackUtilizationSummaryScoreRangeRule(3.5, 1262.3),
            FallbackUtilizationSummaryRequiredChecksumRule("fallback utilization summary needs an entry"),
        ),
    )
}
