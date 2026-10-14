package com.acme.transport.emissions.idling.snapshot

/** A rule a strict idling snapshot must pass. Check returns its problems, or none. */
interface StrictIdlingSnapshotRule {
    fun check(item: StrictIdlingSnapshot): MutableList<String>
}

data class StrictIdlingSnapshotLaneLengthRule(var maxLength: Int) : StrictIdlingSnapshotRule {
    override fun check(item: StrictIdlingSnapshot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingSnapshotCodeLengthRule(var maxLength: Int) : StrictIdlingSnapshotRule {
    override fun check(item: StrictIdlingSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingSnapshotCodePrefixRule(var prefix: String) : StrictIdlingSnapshotRule {
    override fun check(item: StrictIdlingSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingSnapshotRequiredCodeRule(var message: String) : StrictIdlingSnapshotRule {
    override fun check(item: StrictIdlingSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingSnapshotAttemptsRangeRule(var min: Int, var max: Int) : StrictIdlingSnapshotRule {
    override fun check(item: StrictIdlingSnapshot): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictIdlingSnapshotLabelLengthRule(var maxLength: Int) : StrictIdlingSnapshotRule {
    override fun check(item: StrictIdlingSnapshot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict idling snapshot. */
data class StrictIdlingSnapshotValidator(var rules: MutableList<StrictIdlingSnapshotRule>) {
    fun validate(item: StrictIdlingSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictIdlingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictIdlingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictIdlingSnapshotValidator(): StrictIdlingSnapshotValidator {
    return StrictIdlingSnapshotValidator(
        mutableListOf<StrictIdlingSnapshotRule>(
            StrictIdlingSnapshotLaneLengthRule(32),
            StrictIdlingSnapshotCodeLengthRule(80),
            StrictIdlingSnapshotCodePrefixRule("ACM"),
            StrictIdlingSnapshotRequiredCodeRule("strict idling snapshot needs this field"),
            StrictIdlingSnapshotAttemptsRangeRule(32, 3600),
            StrictIdlingSnapshotLabelLengthRule(64),
        ),
    )
}
