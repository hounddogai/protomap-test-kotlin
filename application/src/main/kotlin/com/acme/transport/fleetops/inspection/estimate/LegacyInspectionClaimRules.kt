package com.acme.transport.fleetops.inspection.estimate

/** A rule a legacy inspection claim must pass. Check returns its problems, or none. */
interface LegacyInspectionClaimRule {
    fun check(item: LegacyInspectionClaim): MutableList<String>
}

data class LegacyInspectionClaimCodeLengthRule(var maxLength: Int) : LegacyInspectionClaimRule {
    override fun check(item: LegacyInspectionClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionClaimTitleLengthRule(var maxLength: Int) : LegacyInspectionClaimRule {
    override fun check(item: LegacyInspectionClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionClaimFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : LegacyInspectionClaimRule {
    override fun check(item: LegacyInspectionClaim): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class LegacyInspectionClaimRatioRangeRule(var min: Double, var max: Double) : LegacyInspectionClaimRule {
    override fun check(item: LegacyInspectionClaim): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionClaimRequiredCodeRule(var message: String) : LegacyInspectionClaimRule {
    override fun check(item: LegacyInspectionClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection claim. */
data class LegacyInspectionClaimValidator(var rules: MutableList<LegacyInspectionClaimRule>) {
    fun validate(item: LegacyInspectionClaim): MutableList<String> {
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

    fun requireValid(item: LegacyInspectionClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionClaimValidator(): LegacyInspectionClaimValidator {
    return LegacyInspectionClaimValidator(
        mutableListOf<LegacyInspectionClaimRule>(
            LegacyInspectionClaimCodeLengthRule(255),
            LegacyInspectionClaimTitleLengthRule(255),
            LegacyInspectionClaimFloorValueRangeRule(1.5, 4365.1),
            LegacyInspectionClaimRatioRangeRule(3.5, 2339.1),
            LegacyInspectionClaimRequiredCodeRule("legacy inspection claim needs an entry"),
        ),
    )
}
