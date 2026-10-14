package com.acme.transport.terminal.dockdoor.profile

/** A rule a strict dock door batch must pass. Check returns its problems, or none. */
interface StrictDockDoorBatchRule {
    fun check(item: StrictDockDoorBatch): MutableList<String>
}

data class StrictDockDoorBatchRequiredDescriptionRule(var message: String) : StrictDockDoorBatchRule {
    override fun check(item: StrictDockDoorBatch): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorBatchAmountRangeRule(var min: Double, var max: Double) : StrictDockDoorBatchRule {
    override fun check(item: StrictDockDoorBatch): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class StrictDockDoorBatchCodeLengthRule(var maxLength: Int) : StrictDockDoorBatchRule {
    override fun check(item: StrictDockDoorBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorBatchDescriptionLengthRule(var maxLength: Int) : StrictDockDoorBatchRule {
    override fun check(item: StrictDockDoorBatch): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorBatchNotesLengthRule(var maxLength: Int) : StrictDockDoorBatchRule {
    override fun check(item: StrictDockDoorBatch): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dock door batch. */
data class StrictDockDoorBatchValidator(var rules: MutableList<StrictDockDoorBatchRule>) {
    fun validate(item: StrictDockDoorBatch): MutableList<String> {
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

    fun requireValid(item: StrictDockDoorBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDockDoorBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDockDoorBatchValidator(): StrictDockDoorBatchValidator {
    return StrictDockDoorBatchValidator(
        mutableListOf<StrictDockDoorBatchRule>(
            StrictDockDoorBatchRequiredDescriptionRule("strict dock door batch needs an entry"),
            StrictDockDoorBatchAmountRangeRule(5.8, 4857.8),
            StrictDockDoorBatchCodeLengthRule(80),
            StrictDockDoorBatchDescriptionLengthRule(80),
            StrictDockDoorBatchNotesLengthRule(86400),
        ),
    )
}
