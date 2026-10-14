package com.acme.transport.fleetops.defect.defectforecast

/** A rule a pending defect threshold must pass. Check returns its problems, or none. */
interface PendingDefectThresholdRule {
    fun check(item: PendingDefectThreshold): MutableList<String>
}

data class PendingDefectThresholdCodeLengthRule(var maxLength: Int) : PendingDefectThresholdRule {
    override fun check(item: PendingDefectThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectThresholdCodePrefixRule(var prefix: String) : PendingDefectThresholdRule {
    override fun check(item: PendingDefectThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectThresholdNameLengthRule(var maxLength: Int) : PendingDefectThresholdRule {
    override fun check(item: PendingDefectThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectThresholdVersionRangeRule(var min: Int, var max: Int) : PendingDefectThresholdRule {
    override fun check(item: PendingDefectThreshold): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class PendingDefectThresholdRequiredCodeRule(var message: String) : PendingDefectThresholdRule {
    override fun check(item: PendingDefectThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending defect threshold. */
data class PendingDefectThresholdValidator(var rules: MutableList<PendingDefectThresholdRule>) {
    fun validate(item: PendingDefectThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDefectThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDefectThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDefectThresholdValidator(): PendingDefectThresholdValidator {
    return PendingDefectThresholdValidator(
        mutableListOf<PendingDefectThresholdRule>(
            PendingDefectThresholdCodeLengthRule(80),
            PendingDefectThresholdCodePrefixRule("Q"),
            PendingDefectThresholdNameLengthRule(120),
            PendingDefectThresholdVersionRangeRule(16, 32),
            PendingDefectThresholdRequiredCodeRule("pending defect threshold needs an entry"),
        ),
    )
}
