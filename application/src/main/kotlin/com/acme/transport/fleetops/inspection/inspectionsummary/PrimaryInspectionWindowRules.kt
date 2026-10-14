package com.acme.transport.fleetops.inspection.inspectionsummary

/** A rule a primary inspection window must pass. Check returns its problems, or none. */
interface PrimaryInspectionWindowRule {
    fun check(item: PrimaryInspectionWindow): MutableList<String>
}

data class PrimaryInspectionWindowSourceLengthRule(var maxLength: Int) : PrimaryInspectionWindowRule {
    override fun check(item: PrimaryInspectionWindow): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionWindowActiveGuardRule(var limit: Int) : PrimaryInspectionWindowRule {
    override fun check(item: PrimaryInspectionWindow): MutableList<String> {
        if (item.active && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionWindowCodePrefixRule(var prefix: String) : PrimaryInspectionWindowRule {
    override fun check(item: PrimaryInspectionWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionWindowRatioRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryInspectionWindowRule {
    override fun check(item: PrimaryInspectionWindow): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary inspection window. */
data class PrimaryInspectionWindowValidator(var rules: MutableList<PrimaryInspectionWindowRule>) {
    fun validate(item: PrimaryInspectionWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryInspectionWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryInspectionWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryInspectionWindowValidator(): PrimaryInspectionWindowValidator {
    return PrimaryInspectionWindowValidator(
        mutableListOf<PrimaryInspectionWindowRule>(
            PrimaryInspectionWindowSourceLengthRule(86400),
            PrimaryInspectionWindowActiveGuardRule(16),
            PrimaryInspectionWindowCodePrefixRule("OPS"),
            PrimaryInspectionWindowRatioRangeRule(6.2, 1385.4),
        ),
    )
}
