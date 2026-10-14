package com.acme.transport.freightpay.fuelsurcharge.review

/** A rule a manual fuel surcharge schedule must pass. Check returns its problems, or none. */
interface ManualFuelSurchargeScheduleRule {
    fun check(item: ManualFuelSurchargeSchedule): MutableList<String>
}

data class ManualFuelSurchargeScheduleRatioRangeRule(
    var min: Double,
    var max: Double,
) : ManualFuelSurchargeScheduleRule {
    override fun check(item: ManualFuelSurchargeSchedule): MutableList<String> {
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

data class ManualFuelSurchargeScheduleStopCountRangeRule(
    var min: Int,
    var max: Int,
) : ManualFuelSurchargeScheduleRule {
    override fun check(item: ManualFuelSurchargeSchedule): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class ManualFuelSurchargeScheduleRequiredCodeRule(var message: String) : ManualFuelSurchargeScheduleRule {
    override fun check(item: ManualFuelSurchargeSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualFuelSurchargeScheduleCodeLengthRule(var maxLength: Int) : ManualFuelSurchargeScheduleRule {
    override fun check(item: ManualFuelSurchargeSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual fuel surcharge schedule. */
data class ManualFuelSurchargeScheduleValidator(var rules: MutableList<ManualFuelSurchargeScheduleRule>) {
    fun validate(item: ManualFuelSurchargeSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualFuelSurchargeSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFuelSurchargeSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFuelSurchargeScheduleValidator(): ManualFuelSurchargeScheduleValidator {
    return ManualFuelSurchargeScheduleValidator(
        mutableListOf<ManualFuelSurchargeScheduleRule>(
            ManualFuelSurchargeScheduleRatioRangeRule(3.0, 2134.6),
            ManualFuelSurchargeScheduleStopCountRangeRule(32, 120),
            ManualFuelSurchargeScheduleRequiredCodeRule("manual fuel surcharge schedule needs an entry"),
            ManualFuelSurchargeScheduleCodeLengthRule(16),
        ),
    )
}
