package com.acme.transport.fleetops.defect.profile

/** A rule a scheduled defect profile must pass. Check returns its problems, or none. */
interface ScheduledDefectProfileRule {
    fun check(item: ScheduledDefectProfile): MutableList<String>
}

data class ScheduledDefectProfileRequiredExternalRefRule(var message: String) : ScheduledDefectProfileRule {
    override fun check(item: ScheduledDefectProfile): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectProfileCodeLengthRule(var maxLength: Int) : ScheduledDefectProfileRule {
    override fun check(item: ScheduledDefectProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectProfileNameLengthRule(var maxLength: Int) : ScheduledDefectProfileRule {
    override fun check(item: ScheduledDefectProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectProfileOdometerRangeRule(var min: Int, var max: Int) : ScheduledDefectProfileRule {
    override fun check(item: ScheduledDefectProfile): MutableList<String> {
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

data class ScheduledDefectProfileRequiredCodeRule(var message: String) : ScheduledDefectProfileRule {
    override fun check(item: ScheduledDefectProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled defect profile. */
data class ScheduledDefectProfileValidator(var rules: MutableList<ScheduledDefectProfileRule>) {
    fun validate(item: ScheduledDefectProfile): MutableList<String> {
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

    fun requireValid(item: ScheduledDefectProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDefectProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDefectProfileValidator(): ScheduledDefectProfileValidator {
    return ScheduledDefectProfileValidator(
        mutableListOf<ScheduledDefectProfileRule>(
            ScheduledDefectProfileRequiredExternalRefRule("scheduled defect profile needs an entry"),
            ScheduledDefectProfileCodeLengthRule(1000),
            ScheduledDefectProfileNameLengthRule(3600),
            ScheduledDefectProfileOdometerRangeRule(32, 80),
            ScheduledDefectProfileRequiredCodeRule("scheduled defect profile needs a value"),
        ),
    )
}
