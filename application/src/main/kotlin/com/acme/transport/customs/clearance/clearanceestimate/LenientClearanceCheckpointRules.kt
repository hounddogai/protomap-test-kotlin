package com.acme.transport.customs.clearance.clearanceestimate

/** A rule a lenient clearance checkpoint must pass. Check returns its problems, or none. */
interface LenientClearanceCheckpointRule {
    fun check(item: LenientClearanceCheckpoint): MutableList<String>
}

data class LenientClearanceCheckpointCodeLengthRule(var maxLength: Int) : LenientClearanceCheckpointRule {
    override fun check(item: LenientClearanceCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceCheckpointCodePrefixRule(var prefix: String) : LenientClearanceCheckpointRule {
    override fun check(item: LenientClearanceCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceCheckpointMarginRangeRule(
    var min: Double,
    var max: Double,
) : LenientClearanceCheckpointRule {
    override fun check(item: LenientClearanceCheckpoint): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class LenientClearanceCheckpointRequiredCodeRule(var message: String) : LenientClearanceCheckpointRule {
    override fun check(item: LenientClearanceCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceCheckpointNameLengthRule(var maxLength: Int) : LenientClearanceCheckpointRule {
    override fun check(item: LenientClearanceCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceCheckpointRequiredDockRule(var message: String) : LenientClearanceCheckpointRule {
    override fun check(item: LenientClearanceCheckpoint): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient clearance checkpoint. */
data class LenientClearanceCheckpointValidator(var rules: MutableList<LenientClearanceCheckpointRule>) {
    fun validate(item: LenientClearanceCheckpoint): MutableList<String> {
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

    fun requireValid(item: LenientClearanceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientClearanceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientClearanceCheckpointValidator(): LenientClearanceCheckpointValidator {
    return LenientClearanceCheckpointValidator(
        mutableListOf<LenientClearanceCheckpointRule>(
            LenientClearanceCheckpointCodeLengthRule(86400),
            LenientClearanceCheckpointCodePrefixRule("ACM"),
            LenientClearanceCheckpointMarginRangeRule(4.8, 3580.5),
            LenientClearanceCheckpointRequiredCodeRule("lenient clearance checkpoint needs an entry"),
            LenientClearanceCheckpointNameLengthRule(255),
            LenientClearanceCheckpointRequiredDockRule("lenient clearance checkpoint needs a value"),
        ),
    )
}
