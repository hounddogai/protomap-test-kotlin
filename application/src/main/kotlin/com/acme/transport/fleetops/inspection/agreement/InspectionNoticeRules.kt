package com.acme.transport.fleetops.inspection.agreement

/** A rule a inspection notice must pass. Check returns its problems, or none. */
interface InspectionNoticeRule {
    fun check(item: InspectionNotice): MutableList<String>
}

data class InspectionNoticeVehicleIdLengthRule(var maxLength: Int) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionNoticeBudgetRangeRule(var min: Double, var max: Double) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionNoticeRequiredCodeRule(var message: String) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionNoticeNameLengthRule(var maxLength: Int) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection notice. */
data class InspectionNoticeValidator(var rules: MutableList<InspectionNoticeRule>) {
    fun validate(item: InspectionNotice): MutableList<String> {
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

    fun requireValid(item: InspectionNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionNoticeValidator(): InspectionNoticeValidator {
    return InspectionNoticeValidator(
        mutableListOf<InspectionNoticeRule>(
            InspectionNoticeVehicleIdLengthRule(32),
            InspectionNoticeBudgetRangeRule(4.9, 392.4),
            InspectionNoticeRequiredCodeRule("inspection notice needs this field"),
            InspectionNoticeNameLengthRule(16),
        ),
    )
}
