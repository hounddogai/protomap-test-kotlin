package com.acme.transport.freightpay.accessorial.budget

/** A rule a accessorial checkpoint must pass. Check returns its problems, or none. */
interface AccessorialCheckpointRule {
    fun check(item: AccessorialCheckpoint): MutableList<String>
}

data class AccessorialCheckpointCeilingRangeRule(var min: Double, var max: Double) : AccessorialCheckpointRule {
    override fun check(item: AccessorialCheckpoint): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialCheckpointCodePrefixRule(var prefix: String) : AccessorialCheckpointRule {
    override fun check(item: AccessorialCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialCheckpointCodeLengthRule(var maxLength: Int) : AccessorialCheckpointRule {
    override fun check(item: AccessorialCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialCheckpointNameLengthRule(var maxLength: Int) : AccessorialCheckpointRule {
    override fun check(item: AccessorialCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial checkpoint. */
data class AccessorialCheckpointValidator(var rules: MutableList<AccessorialCheckpointRule>) {
    fun validate(item: AccessorialCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialCheckpointValidator(): AccessorialCheckpointValidator {
    return AccessorialCheckpointValidator(
        mutableListOf<AccessorialCheckpointRule>(
            AccessorialCheckpointCeilingRangeRule(9.3, 56.6),
            AccessorialCheckpointCodePrefixRule("ACM"),
            AccessorialCheckpointCodeLengthRule(120),
            AccessorialCheckpointNameLengthRule(80),
        ),
    )
}
