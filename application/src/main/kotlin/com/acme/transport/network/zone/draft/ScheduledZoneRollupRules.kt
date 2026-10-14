package com.acme.transport.network.zone.draft

/** A rule a scheduled zone rollup must pass. Check returns its problems, or none. */
interface ScheduledZoneRollupRule {
    fun check(item: ScheduledZoneRollup): MutableList<String>
}

data class ScheduledZoneRollupCreatedAtRangeRule(var min: Int, var max: Int) : ScheduledZoneRollupRule {
    override fun check(item: ScheduledZoneRollup): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class ScheduledZoneRollupQuantityRangeRule(var min: Int, var max: Int) : ScheduledZoneRollupRule {
    override fun check(item: ScheduledZoneRollup): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneRollupNameLengthRule(var maxLength: Int) : ScheduledZoneRollupRule {
    override fun check(item: ScheduledZoneRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledZoneRollupRequiredCodeRule(var message: String) : ScheduledZoneRollupRule {
    override fun check(item: ScheduledZoneRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled zone rollup. */
data class ScheduledZoneRollupValidator(var rules: MutableList<ScheduledZoneRollupRule>) {
    fun validate(item: ScheduledZoneRollup): MutableList<String> {
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

    fun requireValid(item: ScheduledZoneRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledZoneRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledZoneRollupValidator(): ScheduledZoneRollupValidator {
    return ScheduledZoneRollupValidator(
        mutableListOf<ScheduledZoneRollupRule>(
            ScheduledZoneRollupCreatedAtRangeRule(64, 255),
            ScheduledZoneRollupQuantityRangeRule(64, 255),
            ScheduledZoneRollupNameLengthRule(3600),
            ScheduledZoneRollupRequiredCodeRule("scheduled zone rollup needs a value"),
        ),
    )
}
