package com.acme.transport.fleetops.serviceplan.planledger

/** A rule a legacy service plan baseline must pass. Check returns its problems, or none. */
interface LegacyServicePlanBaselineRule {
    fun check(item: LegacyServicePlanBaseline): MutableList<String>
}

data class LegacyServicePlanBaselineOwnerIdLengthRule(var maxLength: Int) : LegacyServicePlanBaselineRule {
    override fun check(item: LegacyServicePlanBaseline): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanBaselineCodeLengthRule(var maxLength: Int) : LegacyServicePlanBaselineRule {
    override fun check(item: LegacyServicePlanBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanBaselineNameLengthRule(var maxLength: Int) : LegacyServicePlanBaselineRule {
    override fun check(item: LegacyServicePlanBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyServicePlanBaselineThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LegacyServicePlanBaselineRule {
    override fun check(item: LegacyServicePlanBaseline): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy service plan baseline. */
data class LegacyServicePlanBaselineValidator(var rules: MutableList<LegacyServicePlanBaselineRule>) {
    fun validate(item: LegacyServicePlanBaseline): MutableList<String> {
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

    fun requireValid(item: LegacyServicePlanBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyServicePlanBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyServicePlanBaselineValidator(): LegacyServicePlanBaselineValidator {
    return LegacyServicePlanBaselineValidator(
        mutableListOf<LegacyServicePlanBaselineRule>(
            LegacyServicePlanBaselineOwnerIdLengthRule(32),
            LegacyServicePlanBaselineCodeLengthRule(255),
            LegacyServicePlanBaselineNameLengthRule(86400),
            LegacyServicePlanBaselineThresholdRangeRule(4.7, 522.2),
        ),
    )
}
