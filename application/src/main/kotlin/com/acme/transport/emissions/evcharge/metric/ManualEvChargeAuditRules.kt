package com.acme.transport.emissions.evcharge.metric

/** A rule a manual ev charge audit must pass. Check returns its problems, or none. */
interface ManualEvChargeAuditRule {
    fun check(item: ManualEvChargeAudit): MutableList<String>
}

data class ManualEvChargeAuditRequiredTitleRule(var message: String) : ManualEvChargeAuditRule {
    override fun check(item: ManualEvChargeAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeAuditShiftCodeLengthRule(var maxLength: Int) : ManualEvChargeAuditRule {
    override fun check(item: ManualEvChargeAudit): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeAuditThresholdRangeRule(var min: Double, var max: Double) : ManualEvChargeAuditRule {
    override fun check(item: ManualEvChargeAudit): MutableList<String> {
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

data class ManualEvChargeAuditRequiredLocaleRule(var message: String) : ManualEvChargeAuditRule {
    override fun check(item: ManualEvChargeAudit): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeAuditLocaleLengthRule(var maxLength: Int) : ManualEvChargeAuditRule {
    override fun check(item: ManualEvChargeAudit): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualEvChargeAuditCapacityRangeRule(var min: Int, var max: Int) : ManualEvChargeAuditRule {
    override fun check(item: ManualEvChargeAudit): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual ev charge audit. */
data class ManualEvChargeAuditValidator(var rules: MutableList<ManualEvChargeAuditRule>) {
    fun validate(item: ManualEvChargeAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualEvChargeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualEvChargeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultManualEvChargeAuditValidator(): ManualEvChargeAuditValidator {
    return ManualEvChargeAuditValidator(
        mutableListOf<ManualEvChargeAuditRule>(
            ManualEvChargeAuditRequiredTitleRule("manual ev charge audit needs this field"),
            ManualEvChargeAuditShiftCodeLengthRule(32),
            ManualEvChargeAuditThresholdRangeRule(5.1, 3411.2),
            ManualEvChargeAuditRequiredLocaleRule("manual ev charge audit needs this field"),
            ManualEvChargeAuditLocaleLengthRule(32),
            ManualEvChargeAuditCapacityRangeRule(64, 3600),
        ),
    )
}
