package com.acme.transport.fleetops.defect.checkpoint

/** A rule a defect checkpoint must pass. Check returns its problems, or none. */
interface DefectCheckpointRule {
    fun check(item: DefectCheckpoint): MutableList<String>
}

data class DefectCheckpointCodeLengthRule(var maxLength: Int) : DefectCheckpointRule {
    override fun check(item: DefectCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectCheckpointNameLengthRule(var maxLength: Int) : DefectCheckpointRule {
    override fun check(item: DefectCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectCheckpointRequiredCurrencyRule(var message: String) : DefectCheckpointRule {
    override fun check(item: DefectCheckpoint): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectCheckpointCodePrefixRule(var prefix: String) : DefectCheckpointRule {
    override fun check(item: DefectCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectCheckpointRequiredNameRule(var message: String) : DefectCheckpointRule {
    override fun check(item: DefectCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectCheckpointCreatedAtRangeRule(var min: Int, var max: Int) : DefectCheckpointRule {
    override fun check(item: DefectCheckpoint): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect checkpoint. */
data class DefectCheckpointValidator(var rules: MutableList<DefectCheckpointRule>) {
    fun validate(item: DefectCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectCheckpointValidator(): DefectCheckpointValidator {
    return DefectCheckpointValidator(
        mutableListOf<DefectCheckpointRule>(
            DefectCheckpointCodeLengthRule(64),
            DefectCheckpointNameLengthRule(32),
            DefectCheckpointRequiredCurrencyRule("defect checkpoint needs an entry"),
            DefectCheckpointCodePrefixRule("ACM"),
            DefectCheckpointRequiredNameRule("defect checkpoint needs a value"),
            DefectCheckpointCreatedAtRangeRule(32, 64),
        ),
    )
}
