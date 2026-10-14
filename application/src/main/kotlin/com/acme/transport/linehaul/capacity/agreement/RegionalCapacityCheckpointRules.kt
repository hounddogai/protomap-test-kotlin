package com.acme.transport.linehaul.capacity.agreement

/** A rule a regional capacity checkpoint must pass. Check returns its problems, or none. */
interface RegionalCapacityCheckpointRule {
    fun check(item: RegionalCapacityCheckpoint): MutableList<String>
}

data class RegionalCapacityCheckpointCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCapacityCheckpointRule {
    override fun check(item: RegionalCapacityCheckpoint): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityCheckpointCodePrefixRule(var prefix: String) : RegionalCapacityCheckpointRule {
    override fun check(item: RegionalCapacityCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityCheckpointRequiredNameRule(var message: String) : RegionalCapacityCheckpointRule {
    override fun check(item: RegionalCapacityCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityCheckpointNameLengthRule(var maxLength: Int) : RegionalCapacityCheckpointRule {
    override fun check(item: RegionalCapacityCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCapacityCheckpointDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCapacityCheckpointRule {
    override fun check(item: RegionalCapacityCheckpoint): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional capacity checkpoint. */
data class RegionalCapacityCheckpointValidator(var rules: MutableList<RegionalCapacityCheckpointRule>) {
    fun validate(item: RegionalCapacityCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCapacityCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCapacityCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCapacityCheckpointValidator(): RegionalCapacityCheckpointValidator {
    return RegionalCapacityCheckpointValidator(
        mutableListOf<RegionalCapacityCheckpointRule>(
            RegionalCapacityCheckpointCreatedAtRangeRule(32, 86400),
            RegionalCapacityCheckpointCodePrefixRule("ACM"),
            RegionalCapacityCheckpointRequiredNameRule("regional capacity checkpoint needs a value"),
            RegionalCapacityCheckpointNameLengthRule(86400),
            RegionalCapacityCheckpointDurationMinutesRangeRule(80, 3600),
        ),
    )
}
