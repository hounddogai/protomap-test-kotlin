package com.acme.transport.customs.inspection.draft

/** A rule a weighted inspection rollup must pass. Check returns its problems, or none. */
interface WeightedInspectionRollupRule {
    fun check(item: WeightedInspectionRollup): MutableList<String>
}

data class WeightedInspectionRollupShiftCodeLengthRule(var maxLength: Int) : WeightedInspectionRollupRule {
    override fun check(item: WeightedInspectionRollup): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionRollupOdometerRangeRule(
    var min: Int,
    var max: Int,
) : WeightedInspectionRollupRule {
    override fun check(item: WeightedInspectionRollup): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class WeightedInspectionRollupRequiredShiftCodeRule(var message: String) : WeightedInspectionRollupRule {
    override fun check(item: WeightedInspectionRollup): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionRollupCodePrefixRule(var prefix: String) : WeightedInspectionRollupRule {
    override fun check(item: WeightedInspectionRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted inspection rollup. */
data class WeightedInspectionRollupValidator(var rules: MutableList<WeightedInspectionRollupRule>) {
    fun validate(item: WeightedInspectionRollup): MutableList<String> {
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

    fun requireValid(item: WeightedInspectionRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedInspectionRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedInspectionRollupValidator(): WeightedInspectionRollupValidator {
    return WeightedInspectionRollupValidator(
        mutableListOf<WeightedInspectionRollupRule>(
            WeightedInspectionRollupShiftCodeLengthRule(86400),
            WeightedInspectionRollupOdometerRangeRule(32, 3600),
            WeightedInspectionRollupRequiredShiftCodeRule("weighted inspection rollup needs a value"),
            WeightedInspectionRollupCodePrefixRule("Q"),
        ),
    )
}
