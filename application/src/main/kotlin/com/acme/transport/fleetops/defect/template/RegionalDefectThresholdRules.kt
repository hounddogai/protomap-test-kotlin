package com.acme.transport.fleetops.defect.template

/** A rule a regional defect threshold must pass. Check returns its problems, or none. */
interface RegionalDefectThresholdRule {
    fun check(item: RegionalDefectThreshold): MutableList<String>
}

data class RegionalDefectThresholdCodeLengthRule(var maxLength: Int) : RegionalDefectThresholdRule {
    override fun check(item: RegionalDefectThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectThresholdWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : RegionalDefectThresholdRule {
    override fun check(item: RegionalDefectThreshold): MutableList<String> {
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

data class RegionalDefectThresholdCodePrefixRule(var prefix: String) : RegionalDefectThresholdRule {
    override fun check(item: RegionalDefectThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectThresholdRequiredTitleRule(var message: String) : RegionalDefectThresholdRule {
    override fun check(item: RegionalDefectThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional defect threshold. */
data class RegionalDefectThresholdValidator(var rules: MutableList<RegionalDefectThresholdRule>) {
    fun validate(item: RegionalDefectThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDefectThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDefectThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDefectThresholdValidator(): RegionalDefectThresholdValidator {
    return RegionalDefectThresholdValidator(
        mutableListOf<RegionalDefectThresholdRule>(
            RegionalDefectThresholdCodeLengthRule(16),
            RegionalDefectThresholdWindowDaysRangeRule(32, 3600),
            RegionalDefectThresholdCodePrefixRule("REF"),
            RegionalDefectThresholdRequiredTitleRule("regional defect threshold needs an entry"),
        ),
    )
}
