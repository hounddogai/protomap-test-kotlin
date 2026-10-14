package com.acme.transport.crewing.dutyperiod.target

/** A rule a lenient duty period bundle must pass. Check returns its problems, or none. */
interface LenientDutyPeriodBundleRule {
    fun check(item: LenientDutyPeriodBundle): MutableList<String>
}

data class LenientDutyPeriodBundlePhoneLengthRule(var maxLength: Int) : LenientDutyPeriodBundleRule {
    override fun check(item: LenientDutyPeriodBundle): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodBundleUnitLengthRule(var maxLength: Int) : LenientDutyPeriodBundleRule {
    override fun check(item: LenientDutyPeriodBundle): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodBundleRequiredCodeRule(var message: String) : LenientDutyPeriodBundleRule {
    override fun check(item: LenientDutyPeriodBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient duty period bundle. */
data class LenientDutyPeriodBundleValidator(var rules: MutableList<LenientDutyPeriodBundleRule>) {
    fun validate(item: LenientDutyPeriodBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDutyPeriodBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDutyPeriodBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDutyPeriodBundleValidator(): LenientDutyPeriodBundleValidator {
    return LenientDutyPeriodBundleValidator(
        mutableListOf<LenientDutyPeriodBundleRule>(
            LenientDutyPeriodBundlePhoneLengthRule(32),
            LenientDutyPeriodBundleUnitLengthRule(16),
            LenientDutyPeriodBundleRequiredCodeRule("lenient duty period bundle needs a value"),
        ),
    )
}
