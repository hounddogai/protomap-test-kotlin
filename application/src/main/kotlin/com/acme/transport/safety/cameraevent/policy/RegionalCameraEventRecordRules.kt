package com.acme.transport.safety.cameraevent.policy

/** A rule a regional camera event record must pass. Check returns its problems, or none. */
interface RegionalCameraEventRecordRule {
    fun check(item: RegionalCameraEventRecord): MutableList<String>
}

data class RegionalCameraEventRecordMarginRangeRule(
    var min: Double,
    var max: Double,
) : RegionalCameraEventRecordRule {
    override fun check(item: RegionalCameraEventRecord): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventRecordCodePrefixRule(var prefix: String) : RegionalCameraEventRecordRule {
    override fun check(item: RegionalCameraEventRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventRecordTitleLengthRule(var maxLength: Int) : RegionalCameraEventRecordRule {
    override fun check(item: RegionalCameraEventRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventRecordCodeLengthRule(var maxLength: Int) : RegionalCameraEventRecordRule {
    override fun check(item: RegionalCameraEventRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventRecordRequiredTitleRule(var message: String) : RegionalCameraEventRecordRule {
    override fun check(item: RegionalCameraEventRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional camera event record. */
data class RegionalCameraEventRecordValidator(var rules: MutableList<RegionalCameraEventRecordRule>) {
    fun validate(item: RegionalCameraEventRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCameraEventRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCameraEventRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCameraEventRecordValidator(): RegionalCameraEventRecordValidator {
    return RegionalCameraEventRecordValidator(
        mutableListOf<RegionalCameraEventRecordRule>(
            RegionalCameraEventRecordMarginRangeRule(8.7, 252.1),
            RegionalCameraEventRecordCodePrefixRule("ACM"),
            RegionalCameraEventRecordTitleLengthRule(16),
            RegionalCameraEventRecordCodeLengthRule(3600),
            RegionalCameraEventRecordRequiredTitleRule("regional camera event record needs an entry"),
        ),
    )
}
