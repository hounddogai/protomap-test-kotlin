package com.acme.transport.safety.nearmiss.notice

/** A rule a regional near miss audit must pass. Check returns its problems, or none. */
interface RegionalNearMissAuditRule {
    fun check(item: RegionalNearMissAudit): MutableList<String>
}

data class RegionalNearMissAuditRequiredShiftCodeRule(var message: String) : RegionalNearMissAuditRule {
    override fun check(item: RegionalNearMissAudit): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalNearMissAuditRequiredNameRule(var message: String) : RegionalNearMissAuditRule {
    override fun check(item: RegionalNearMissAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalNearMissAuditShiftCodeLengthRule(var maxLength: Int) : RegionalNearMissAuditRule {
    override fun check(item: RegionalNearMissAudit): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNearMissAuditLimitRangeRule(var min: Int, var max: Int) : RegionalNearMissAuditRule {
    override fun check(item: RegionalNearMissAudit): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional near miss audit. */
data class RegionalNearMissAuditValidator(var rules: MutableList<RegionalNearMissAuditRule>) {
    fun validate(item: RegionalNearMissAudit): MutableList<String> {
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

    fun requireValid(item: RegionalNearMissAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalNearMissAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalNearMissAuditValidator(): RegionalNearMissAuditValidator {
    return RegionalNearMissAuditValidator(
        mutableListOf<RegionalNearMissAuditRule>(
            RegionalNearMissAuditRequiredShiftCodeRule("regional near miss audit needs a value"),
            RegionalNearMissAuditRequiredNameRule("regional near miss audit needs this field"),
            RegionalNearMissAuditShiftCodeLengthRule(86400),
            RegionalNearMissAuditLimitRangeRule(80, 86400),
        ),
    )
}
