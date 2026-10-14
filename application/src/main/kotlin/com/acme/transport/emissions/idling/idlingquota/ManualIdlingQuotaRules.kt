package com.acme.transport.emissions.idling.idlingquota

/** A rule a manual idling quota must pass. Check returns its problems, or none. */
interface ManualIdlingQuotaRule {
    fun check(item: ManualIdlingQuota): MutableList<String>
}

data class ManualIdlingQuotaRequiredShiftCodeRule(var message: String) : ManualIdlingQuotaRule {
    override fun check(item: ManualIdlingQuota): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingQuotaRequiredCodeRule(var message: String) : ManualIdlingQuotaRule {
    override fun check(item: ManualIdlingQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingQuotaRequiredNameRule(var message: String) : ManualIdlingQuotaRule {
    override fun check(item: ManualIdlingQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualIdlingQuotaRetriesRangeRule(var min: Int, var max: Int) : ManualIdlingQuotaRule {
    override fun check(item: ManualIdlingQuota): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual idling quota. */
data class ManualIdlingQuotaValidator(var rules: MutableList<ManualIdlingQuotaRule>) {
    fun validate(item: ManualIdlingQuota): MutableList<String> {
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

    fun requireValid(item: ManualIdlingQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualIdlingQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultManualIdlingQuotaValidator(): ManualIdlingQuotaValidator {
    return ManualIdlingQuotaValidator(
        mutableListOf<ManualIdlingQuotaRule>(
            ManualIdlingQuotaRequiredShiftCodeRule("manual idling quota needs a value"),
            ManualIdlingQuotaRequiredCodeRule("manual idling quota needs a value"),
            ManualIdlingQuotaRequiredNameRule("manual idling quota needs a value"),
            ManualIdlingQuotaRetriesRangeRule(16, 120),
        ),
    )
}
