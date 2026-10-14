package com.acme.transport.linehaul.tender.slot

/** A rule a manual tender checkpoint must pass. Check returns its problems, or none. */
interface ManualTenderCheckpointRule {
    fun check(item: ManualTenderCheckpoint): MutableList<String>
}

data class ManualTenderCheckpointContactEmailLengthRule(var maxLength: Int) : ManualTenderCheckpointRule {
    override fun check(item: ManualTenderCheckpoint): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderCheckpointCodePrefixRule(var prefix: String) : ManualTenderCheckpointRule {
    override fun check(item: ManualTenderCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderCheckpointBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ManualTenderCheckpointRule {
    override fun check(item: ManualTenderCheckpoint): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderCheckpointRequiredSegmentRule(var message: String) : ManualTenderCheckpointRule {
    override fun check(item: ManualTenderCheckpoint): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tender checkpoint. */
data class ManualTenderCheckpointValidator(var rules: MutableList<ManualTenderCheckpointRule>) {
    fun validate(item: ManualTenderCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTenderCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTenderCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTenderCheckpointValidator(): ManualTenderCheckpointValidator {
    return ManualTenderCheckpointValidator(
        mutableListOf<ManualTenderCheckpointRule>(
            ManualTenderCheckpointContactEmailLengthRule(255),
            ManualTenderCheckpointCodePrefixRule("REF"),
            ManualTenderCheckpointBudgetRangeRule(4.1, 2575.6),
            ManualTenderCheckpointRequiredSegmentRule("manual tender checkpoint needs this field"),
        ),
    )
}
