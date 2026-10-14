package com.acme.transport.emissions.evcharge.metric

/** A rule a fallback ev charge entry must pass. Check returns its problems, or none. */
interface FallbackEvChargeEntryRule {
    fun check(item: FallbackEvChargeEntry): MutableList<String>
}

data class FallbackEvChargeEntryLimitRangeRule(var min: Int, var max: Int) : FallbackEvChargeEntryRule {
    override fun check(item: FallbackEvChargeEntry): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class FallbackEvChargeEntryRatioRangeRule(var min: Double, var max: Double) : FallbackEvChargeEntryRule {
    override fun check(item: FallbackEvChargeEntry): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeEntryTrailerIdLengthRule(var maxLength: Int) : FallbackEvChargeEntryRule {
    override fun check(item: FallbackEvChargeEntry): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeEntryRequiredLabelRule(var message: String) : FallbackEvChargeEntryRule {
    override fun check(item: FallbackEvChargeEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeEntryCodeLengthRule(var maxLength: Int) : FallbackEvChargeEntryRule {
    override fun check(item: FallbackEvChargeEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback ev charge entry. */
data class FallbackEvChargeEntryValidator(var rules: MutableList<FallbackEvChargeEntryRule>) {
    fun validate(item: FallbackEvChargeEntry): MutableList<String> {
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

    fun requireValid(item: FallbackEvChargeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEvChargeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEvChargeEntryValidator(): FallbackEvChargeEntryValidator {
    return FallbackEvChargeEntryValidator(
        mutableListOf<FallbackEvChargeEntryRule>(
            FallbackEvChargeEntryLimitRangeRule(32, 3600),
            FallbackEvChargeEntryRatioRangeRule(3.1, 1572.8),
            FallbackEvChargeEntryTrailerIdLengthRule(120),
            FallbackEvChargeEntryRequiredLabelRule("fallback ev charge entry needs an entry"),
            FallbackEvChargeEntryCodeLengthRule(1000),
        ),
    )
}
