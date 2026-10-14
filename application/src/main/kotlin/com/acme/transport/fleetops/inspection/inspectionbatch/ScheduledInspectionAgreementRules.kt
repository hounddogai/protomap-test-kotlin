package com.acme.transport.fleetops.inspection.inspectionbatch

/** A rule a scheduled inspection agreement must pass. Check returns its problems, or none. */
interface ScheduledInspectionAgreementRule {
    fun check(item: ScheduledInspectionAgreement): MutableList<String>
}

data class ScheduledInspectionAgreementVisibleGuardRule(var limit: Int) : ScheduledInspectionAgreementRule {
    override fun check(item: ScheduledInspectionAgreement): MutableList<String> {
        if (item.visible && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionAgreementNameLengthRule(var maxLength: Int) : ScheduledInspectionAgreementRule {
    override fun check(item: ScheduledInspectionAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionAgreementWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledInspectionAgreementRule {
    override fun check(item: ScheduledInspectionAgreement): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionAgreementRequiredCodeRule(
    var message: String,
) : ScheduledInspectionAgreementRule {
    override fun check(item: ScheduledInspectionAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionAgreementCodePrefixRule(var prefix: String) : ScheduledInspectionAgreementRule {
    override fun check(item: ScheduledInspectionAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled inspection agreement. */
data class ScheduledInspectionAgreementValidator(var rules: MutableList<ScheduledInspectionAgreementRule>) {
    fun validate(item: ScheduledInspectionAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledInspectionAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledInspectionAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledInspectionAgreementValidator(): ScheduledInspectionAgreementValidator {
    return ScheduledInspectionAgreementValidator(
        mutableListOf<ScheduledInspectionAgreementRule>(
            ScheduledInspectionAgreementVisibleGuardRule(32),
            ScheduledInspectionAgreementNameLengthRule(1000),
            ScheduledInspectionAgreementWindowDaysRangeRule(32, 64),
            ScheduledInspectionAgreementRequiredCodeRule("scheduled inspection agreement needs a value"),
            ScheduledInspectionAgreementCodePrefixRule("X"),
        ),
    )
}
