package com.acme.transport.customs.inspection.slot

/** A rule a fallback inspection override must pass. Check returns its problems, or none. */
interface FallbackInspectionOverrideRule {
    fun check(item: FallbackInspectionOverride): MutableList<String>
}

data class FallbackInspectionOverrideRequiredOwnerIdRule(var message: String) : FallbackInspectionOverrideRule {
    override fun check(item: FallbackInspectionOverride): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionOverrideCostRangeRule(
    var min: Double,
    var max: Double,
) : FallbackInspectionOverrideRule {
    override fun check(item: FallbackInspectionOverride): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class FallbackInspectionOverrideSummaryLengthRule(var maxLength: Int) : FallbackInspectionOverrideRule {
    override fun check(item: FallbackInspectionOverride): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionOverrideVersionRangeRule(
    var min: Int,
    var max: Int,
) : FallbackInspectionOverrideRule {
    override fun check(item: FallbackInspectionOverride): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback inspection override. */
data class FallbackInspectionOverrideValidator(var rules: MutableList<FallbackInspectionOverrideRule>) {
    fun validate(item: FallbackInspectionOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackInspectionOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackInspectionOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackInspectionOverrideValidator(): FallbackInspectionOverrideValidator {
    return FallbackInspectionOverrideValidator(
        mutableListOf<FallbackInspectionOverrideRule>(
            FallbackInspectionOverrideRequiredOwnerIdRule("fallback inspection override needs this field"),
            FallbackInspectionOverrideCostRangeRule(5.5, 1921.9),
            FallbackInspectionOverrideSummaryLengthRule(3600),
            FallbackInspectionOverrideVersionRangeRule(120, 86400),
        ),
    )
}
