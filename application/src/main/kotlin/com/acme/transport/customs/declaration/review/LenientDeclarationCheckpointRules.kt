package com.acme.transport.customs.declaration.review

/** A rule a lenient declaration checkpoint must pass. Check returns its problems, or none. */
interface LenientDeclarationCheckpointRule {
    fun check(item: LenientDeclarationCheckpoint): MutableList<String>
}

data class LenientDeclarationCheckpointSequenceRangeRule(
    var min: Int,
    var max: Int,
) : LenientDeclarationCheckpointRule {
    override fun check(item: LenientDeclarationCheckpoint): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDeclarationCheckpointRequiredVehicleIdRule(
    var message: String,
) : LenientDeclarationCheckpointRule {
    override fun check(item: LenientDeclarationCheckpoint): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDeclarationCheckpointLabelLengthRule(var maxLength: Int) : LenientDeclarationCheckpointRule {
    override fun check(item: LenientDeclarationCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDeclarationCheckpointRequiredReviewerRule(
    var message: String,
) : LenientDeclarationCheckpointRule {
    override fun check(item: LenientDeclarationCheckpoint): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDeclarationCheckpointCodeLengthRule(var maxLength: Int) : LenientDeclarationCheckpointRule {
    override fun check(item: LenientDeclarationCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient declaration checkpoint. */
data class LenientDeclarationCheckpointValidator(var rules: MutableList<LenientDeclarationCheckpointRule>) {
    fun validate(item: LenientDeclarationCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDeclarationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDeclarationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDeclarationCheckpointValidator(): LenientDeclarationCheckpointValidator {
    return LenientDeclarationCheckpointValidator(
        mutableListOf<LenientDeclarationCheckpointRule>(
            LenientDeclarationCheckpointSequenceRangeRule(255, 86400),
            LenientDeclarationCheckpointRequiredVehicleIdRule("lenient declaration checkpoint needs this field"),
            LenientDeclarationCheckpointLabelLengthRule(255),
            LenientDeclarationCheckpointRequiredReviewerRule("lenient declaration checkpoint needs this field"),
            LenientDeclarationCheckpointCodeLengthRule(64),
        ),
    )
}
