package com.acme.transport.lastmile.locker.score

/** A rule a manual locker quota must pass. Check returns its problems, or none. */
interface ManualLockerQuotaRule {
    fun check(item: ManualLockerQuota): MutableList<String>
}

data class ManualLockerQuotaRequiredCodeRule(var message: String) : ManualLockerQuotaRule {
    override fun check(item: ManualLockerQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualLockerQuotaCodeLengthRule(var maxLength: Int) : ManualLockerQuotaRule {
    override fun check(item: ManualLockerQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualLockerQuotaBudgetRangeRule(var min: Double, var max: Double) : ManualLockerQuotaRule {
    override fun check(item: ManualLockerQuota): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ManualLockerQuotaCodePrefixRule(var prefix: String) : ManualLockerQuotaRule {
    override fun check(item: ManualLockerQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual locker quota. */
data class ManualLockerQuotaValidator(var rules: MutableList<ManualLockerQuotaRule>) {
    fun validate(item: ManualLockerQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualLockerQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualLockerQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultManualLockerQuotaValidator(): ManualLockerQuotaValidator {
    return ManualLockerQuotaValidator(
        mutableListOf<ManualLockerQuotaRule>(
            ManualLockerQuotaRequiredCodeRule("manual locker quota needs an entry"),
            ManualLockerQuotaCodeLengthRule(1000),
            ManualLockerQuotaBudgetRangeRule(1.0, 4150.0),
            ManualLockerQuotaCodePrefixRule("X"),
        ),
    )
}
