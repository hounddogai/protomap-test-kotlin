package com.acme.transport.terminal.crossdock.schedule

/** A rule a regional cross dock quota must pass. Check returns its problems, or none. */
interface RegionalCrossDockQuotaRule {
    fun check(item: RegionalCrossDockQuota): MutableList<String>
}

data class RegionalCrossDockQuotaCodeLengthRule(var maxLength: Int) : RegionalCrossDockQuotaRule {
    override fun check(item: RegionalCrossDockQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockQuotaLabelLengthRule(var maxLength: Int) : RegionalCrossDockQuotaRule {
    override fun check(item: RegionalCrossDockQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockQuotaRequiredCodeRule(var message: String) : RegionalCrossDockQuotaRule {
    override fun check(item: RegionalCrossDockQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockQuotaWindowDaysRangeRule(var min: Int, var max: Int) : RegionalCrossDockQuotaRule {
    override fun check(item: RegionalCrossDockQuota): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class RegionalCrossDockQuotaRequiredLabelRule(var message: String) : RegionalCrossDockQuotaRule {
    override fun check(item: RegionalCrossDockQuota): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCrossDockQuotaCodePrefixRule(var prefix: String) : RegionalCrossDockQuotaRule {
    override fun check(item: RegionalCrossDockQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional cross dock quota. */
data class RegionalCrossDockQuotaValidator(var rules: MutableList<RegionalCrossDockQuotaRule>) {
    fun validate(item: RegionalCrossDockQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCrossDockQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCrossDockQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCrossDockQuotaValidator(): RegionalCrossDockQuotaValidator {
    return RegionalCrossDockQuotaValidator(
        mutableListOf<RegionalCrossDockQuotaRule>(
            RegionalCrossDockQuotaCodeLengthRule(64),
            RegionalCrossDockQuotaLabelLengthRule(255),
            RegionalCrossDockQuotaRequiredCodeRule("regional cross dock quota needs a value"),
            RegionalCrossDockQuotaWindowDaysRangeRule(1000, 3600),
            RegionalCrossDockQuotaRequiredLabelRule("regional cross dock quota needs this field"),
            RegionalCrossDockQuotaCodePrefixRule("REF"),
        ),
    )
}
