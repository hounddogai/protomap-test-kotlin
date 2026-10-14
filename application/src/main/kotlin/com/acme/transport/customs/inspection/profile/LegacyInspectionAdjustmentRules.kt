package com.acme.transport.customs.inspection.profile

/** A rule a legacy inspection adjustment must pass. Check returns its problems, or none. */
interface LegacyInspectionAdjustmentRule {
    fun check(item: LegacyInspectionAdjustment): MutableList<String>
}

data class LegacyInspectionAdjustmentShiftCodeLengthRule(var maxLength: Int) : LegacyInspectionAdjustmentRule {
    override fun check(item: LegacyInspectionAdjustment): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionAdjustmentRateRangeRule(
    var min: Double,
    var max: Double,
) : LegacyInspectionAdjustmentRule {
    override fun check(item: LegacyInspectionAdjustment): MutableList<String> {
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

data class LegacyInspectionAdjustmentRequiredTitleRule(var message: String) : LegacyInspectionAdjustmentRule {
    override fun check(item: LegacyInspectionAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionAdjustmentTitleLengthRule(var maxLength: Int) : LegacyInspectionAdjustmentRule {
    override fun check(item: LegacyInspectionAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionAdjustmentCodeLengthRule(var maxLength: Int) : LegacyInspectionAdjustmentRule {
    override fun check(item: LegacyInspectionAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionAdjustmentRequiredShiftCodeRule(
    var message: String,
) : LegacyInspectionAdjustmentRule {
    override fun check(item: LegacyInspectionAdjustment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection adjustment. */
data class LegacyInspectionAdjustmentValidator(var rules: MutableList<LegacyInspectionAdjustmentRule>) {
    fun validate(item: LegacyInspectionAdjustment): MutableList<String> {
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

    fun requireValid(item: LegacyInspectionAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionAdjustmentValidator(): LegacyInspectionAdjustmentValidator {
    return LegacyInspectionAdjustmentValidator(
        mutableListOf<LegacyInspectionAdjustmentRule>(
            LegacyInspectionAdjustmentShiftCodeLengthRule(80),
            LegacyInspectionAdjustmentRateRangeRule(4.6, 4124.5),
            LegacyInspectionAdjustmentRequiredTitleRule("legacy inspection adjustment needs a value"),
            LegacyInspectionAdjustmentTitleLengthRule(120),
            LegacyInspectionAdjustmentCodeLengthRule(32),
            LegacyInspectionAdjustmentRequiredShiftCodeRule("legacy inspection adjustment needs an entry"),
        ),
    )
}
