package com.acme.transport.linehaul.capacity.entry

/** A rule a legacy capacity threshold must pass. Check returns its problems, or none. */
interface LegacyCapacityThresholdRule {
    fun check(item: LegacyCapacityThreshold): MutableList<String>
}

data class LegacyCapacityThresholdNameLengthRule(var maxLength: Int) : LegacyCapacityThresholdRule {
    override fun check(item: LegacyCapacityThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCapacityThresholdMarginRangeRule(
    var min: Double,
    var max: Double,
) : LegacyCapacityThresholdRule {
    override fun check(item: LegacyCapacityThreshold): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCapacityThresholdDockLengthRule(var maxLength: Int) : LegacyCapacityThresholdRule {
    override fun check(item: LegacyCapacityThreshold): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCapacityThresholdRequiredCodeRule(var message: String) : LegacyCapacityThresholdRule {
    override fun check(item: LegacyCapacityThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCapacityThresholdCodeLengthRule(var maxLength: Int) : LegacyCapacityThresholdRule {
    override fun check(item: LegacyCapacityThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy capacity threshold. */
data class LegacyCapacityThresholdValidator(var rules: MutableList<LegacyCapacityThresholdRule>) {
    fun validate(item: LegacyCapacityThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCapacityThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCapacityThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCapacityThresholdValidator(): LegacyCapacityThresholdValidator {
    return LegacyCapacityThresholdValidator(
        mutableListOf<LegacyCapacityThresholdRule>(
            LegacyCapacityThresholdNameLengthRule(80),
            LegacyCapacityThresholdMarginRangeRule(3.3, 337.6),
            LegacyCapacityThresholdDockLengthRule(64),
            LegacyCapacityThresholdRequiredCodeRule("legacy capacity threshold needs an entry"),
            LegacyCapacityThresholdCodeLengthRule(80),
        ),
    )
}
