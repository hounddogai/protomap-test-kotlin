package com.acme.transport.fleetops.inspection.rollup

/** A rule a manual inspection override must pass. Check returns its problems, or none. */
interface ManualInspectionOverrideRule {
    fun check(item: ManualInspectionOverride): MutableList<String>
}

data class ManualInspectionOverrideRequiredNameRule(var message: String) : ManualInspectionOverrideRule {
    override fun check(item: ManualInspectionOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionOverrideRateRangeRule(
    var min: Double,
    var max: Double,
) : ManualInspectionOverrideRule {
    override fun check(item: ManualInspectionOverride): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class ManualInspectionOverrideNameLengthRule(var maxLength: Int) : ManualInspectionOverrideRule {
    override fun check(item: ManualInspectionOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionOverrideCodePrefixRule(var prefix: String) : ManualInspectionOverrideRule {
    override fun check(item: ManualInspectionOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionOverrideLimitRangeRule(var min: Int, var max: Int) : ManualInspectionOverrideRule {
    override fun check(item: ManualInspectionOverride): MutableList<String> {
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

/** Runs every rule of a manual inspection override. */
data class ManualInspectionOverrideValidator(var rules: MutableList<ManualInspectionOverrideRule>) {
    fun validate(item: ManualInspectionOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualInspectionOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualInspectionOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultManualInspectionOverrideValidator(): ManualInspectionOverrideValidator {
    return ManualInspectionOverrideValidator(
        mutableListOf<ManualInspectionOverrideRule>(
            ManualInspectionOverrideRequiredNameRule("manual inspection override needs an entry"),
            ManualInspectionOverrideRateRangeRule(4.5, 4745.9),
            ManualInspectionOverrideNameLengthRule(80),
            ManualInspectionOverrideCodePrefixRule("OPS"),
            ManualInspectionOverrideLimitRangeRule(16, 64),
        ),
    )
}
