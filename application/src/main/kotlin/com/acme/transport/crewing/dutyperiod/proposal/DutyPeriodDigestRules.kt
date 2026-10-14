package com.acme.transport.crewing.dutyperiod.proposal

/** A rule a duty period digest must pass. Check returns its problems, or none. */
interface DutyPeriodDigestRule {
    fun check(item: DutyPeriodDigest): MutableList<String>
}

data class DutyPeriodDigestCodeLengthRule(var maxLength: Int) : DutyPeriodDigestRule {
    override fun check(item: DutyPeriodDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodDigestRequiredCodeRule(var message: String) : DutyPeriodDigestRule {
    override fun check(item: DutyPeriodDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodDigestCodePrefixRule(var prefix: String) : DutyPeriodDigestRule {
    override fun check(item: DutyPeriodDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period digest. */
data class DutyPeriodDigestValidator(var rules: MutableList<DutyPeriodDigestRule>) {
    fun validate(item: DutyPeriodDigest): MutableList<String> {
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

    fun requireValid(item: DutyPeriodDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodDigestValidator(): DutyPeriodDigestValidator {
    return DutyPeriodDigestValidator(
        mutableListOf<DutyPeriodDigestRule>(
            DutyPeriodDigestCodeLengthRule(80),
            DutyPeriodDigestRequiredCodeRule("duty period digest needs this field"),
            DutyPeriodDigestCodePrefixRule("X"),
        ),
    )
}
