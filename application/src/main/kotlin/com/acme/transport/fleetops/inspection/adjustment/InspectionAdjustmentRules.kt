package com.acme.transport.fleetops.inspection.adjustment

/** A rule a inspection adjustment must pass. Check returns its problems, or none. */
interface InspectionAdjustmentRule {
    fun check(item: InspectionAdjustment): MutableList<String>
}

data class InspectionAdjustmentCodePrefixRule(var prefix: String) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAdjustmentCodeLengthRule(var maxLength: Int) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAdjustmentSummaryLengthRule(var maxLength: Int) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAdjustmentRequiredChannelRule(var message: String) : InspectionAdjustmentRule {
    override fun check(item: InspectionAdjustment): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection adjustment. */
data class InspectionAdjustmentValidator(var rules: MutableList<InspectionAdjustmentRule>) {
    fun validate(item: InspectionAdjustment): MutableList<String> {
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

    fun requireValid(item: InspectionAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionAdjustmentValidator(): InspectionAdjustmentValidator {
    return InspectionAdjustmentValidator(
        mutableListOf<InspectionAdjustmentRule>(
            InspectionAdjustmentCodePrefixRule("X"),
            InspectionAdjustmentCodeLengthRule(64),
            InspectionAdjustmentSummaryLengthRule(64),
            InspectionAdjustmentRequiredChannelRule("inspection adjustment needs this field"),
        ),
    )
}
