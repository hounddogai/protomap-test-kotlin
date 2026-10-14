package com.acme.transport.safety.incident.baseline

/** A rule a incident quota must pass. Check returns its problems, or none. */
interface IncidentQuotaRule {
    fun check(item: IncidentQuota): MutableList<String>
}

data class IncidentQuotaCodePrefixRule(var prefix: String) : IncidentQuotaRule {
    override fun check(item: IncidentQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentQuotaRequiredPhoneRule(var message: String) : IncidentQuotaRule {
    override fun check(item: IncidentQuota): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentQuotaNameLengthRule(var maxLength: Int) : IncidentQuotaRule {
    override fun check(item: IncidentQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentQuotaLaneLengthRule(var maxLength: Int) : IncidentQuotaRule {
    override fun check(item: IncidentQuota): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentQuotaThresholdRangeRule(var min: Double, var max: Double) : IncidentQuotaRule {
    override fun check(item: IncidentQuota): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentQuotaRequiredCodeRule(var message: String) : IncidentQuotaRule {
    override fun check(item: IncidentQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident quota. */
data class IncidentQuotaValidator(var rules: MutableList<IncidentQuotaRule>) {
    fun validate(item: IncidentQuota): MutableList<String> {
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

    fun requireValid(item: IncidentQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentQuotaValidator(): IncidentQuotaValidator {
    return IncidentQuotaValidator(
        mutableListOf<IncidentQuotaRule>(
            IncidentQuotaCodePrefixRule("ACM"),
            IncidentQuotaRequiredPhoneRule("incident quota needs an entry"),
            IncidentQuotaNameLengthRule(1000),
            IncidentQuotaLaneLengthRule(120),
            IncidentQuotaThresholdRangeRule(0.3, 3898.6),
            IncidentQuotaRequiredCodeRule("incident quota needs an entry"),
        ),
    )
}
