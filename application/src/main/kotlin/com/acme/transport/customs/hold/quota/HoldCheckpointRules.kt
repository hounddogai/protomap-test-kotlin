package com.acme.transport.customs.hold.quota

/** A rule a hold checkpoint must pass. Check returns its problems, or none. */
interface HoldCheckpointRule {
    fun check(item: HoldCheckpoint): MutableList<String>
}

data class HoldCheckpointRequiredCodeRule(var message: String) : HoldCheckpointRule {
    override fun check(item: HoldCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldCheckpointEndsAtRangeRule(var min: Int, var max: Int) : HoldCheckpointRule {
    override fun check(item: HoldCheckpoint): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldCheckpointRequiredNameRule(var message: String) : HoldCheckpointRule {
    override fun check(item: HoldCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold checkpoint. */
data class HoldCheckpointValidator(var rules: MutableList<HoldCheckpointRule>) {
    fun validate(item: HoldCheckpoint): MutableList<String> {
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

    fun requireValid(item: HoldCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldCheckpointValidator(): HoldCheckpointValidator {
    return HoldCheckpointValidator(
        mutableListOf<HoldCheckpointRule>(
            HoldCheckpointRequiredCodeRule("hold checkpoint needs this field"),
            HoldCheckpointEndsAtRangeRule(32, 120),
            HoldCheckpointRequiredNameRule("hold checkpoint needs a value"),
        ),
    )
}
