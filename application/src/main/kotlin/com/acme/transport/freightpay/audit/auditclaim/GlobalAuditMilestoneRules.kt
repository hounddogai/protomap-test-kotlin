package com.acme.transport.freightpay.audit.auditclaim

/** A rule a global audit milestone must pass. Check returns its problems, or none. */
interface GlobalAuditMilestoneRule {
    fun check(item: GlobalAuditMilestone): MutableList<String>
}

data class GlobalAuditMilestoneBatchSizeRangeRule(var min: Int, var max: Int) : GlobalAuditMilestoneRule {
    override fun check(item: GlobalAuditMilestone): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditMilestoneCodeLengthRule(var maxLength: Int) : GlobalAuditMilestoneRule {
    override fun check(item: GlobalAuditMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditMilestoneRequiredLabelRule(var message: String) : GlobalAuditMilestoneRule {
    override fun check(item: GlobalAuditMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAuditMilestoneShiftCodeLengthRule(var maxLength: Int) : GlobalAuditMilestoneRule {
    override fun check(item: GlobalAuditMilestone): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global audit milestone. */
data class GlobalAuditMilestoneValidator(var rules: MutableList<GlobalAuditMilestoneRule>) {
    fun validate(item: GlobalAuditMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalAuditMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAuditMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAuditMilestoneValidator(): GlobalAuditMilestoneValidator {
    return GlobalAuditMilestoneValidator(
        mutableListOf<GlobalAuditMilestoneRule>(
            GlobalAuditMilestoneBatchSizeRangeRule(16, 120),
            GlobalAuditMilestoneCodeLengthRule(3600),
            GlobalAuditMilestoneRequiredLabelRule("global audit milestone needs a value"),
            GlobalAuditMilestoneShiftCodeLengthRule(3600),
        ),
    )
}
