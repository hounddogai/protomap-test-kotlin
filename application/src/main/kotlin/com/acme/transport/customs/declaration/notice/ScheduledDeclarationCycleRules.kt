package com.acme.transport.customs.declaration.notice

/** A rule a scheduled declaration cycle must pass. Check returns its problems, or none. */
interface ScheduledDeclarationCycleRule {
    fun check(item: ScheduledDeclarationCycle): MutableList<String>
}

data class ScheduledDeclarationCycleTitleLengthRule(var maxLength: Int) : ScheduledDeclarationCycleRule {
    override fun check(item: ScheduledDeclarationCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDeclarationCycleCodePrefixRule(var prefix: String) : ScheduledDeclarationCycleRule {
    override fun check(item: ScheduledDeclarationCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDeclarationCycleDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDeclarationCycleRule {
    override fun check(item: ScheduledDeclarationCycle): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class ScheduledDeclarationCycleRequiredTitleRule(var message: String) : ScheduledDeclarationCycleRule {
    override fun check(item: ScheduledDeclarationCycle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDeclarationCycleRequiredCodeRule(var message: String) : ScheduledDeclarationCycleRule {
    override fun check(item: ScheduledDeclarationCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDeclarationCycleCodeLengthRule(var maxLength: Int) : ScheduledDeclarationCycleRule {
    override fun check(item: ScheduledDeclarationCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled declaration cycle. */
data class ScheduledDeclarationCycleValidator(var rules: MutableList<ScheduledDeclarationCycleRule>) {
    fun validate(item: ScheduledDeclarationCycle): MutableList<String> {
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

    fun requireValid(item: ScheduledDeclarationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDeclarationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDeclarationCycleValidator(): ScheduledDeclarationCycleValidator {
    return ScheduledDeclarationCycleValidator(
        mutableListOf<ScheduledDeclarationCycleRule>(
            ScheduledDeclarationCycleTitleLengthRule(80),
            ScheduledDeclarationCycleCodePrefixRule("REF"),
            ScheduledDeclarationCycleDistanceKmRangeRule(7.6, 4448.8),
            ScheduledDeclarationCycleRequiredTitleRule("scheduled declaration cycle needs a value"),
            ScheduledDeclarationCycleRequiredCodeRule("scheduled declaration cycle needs a value"),
            ScheduledDeclarationCycleCodeLengthRule(16),
        ),
    )
}
