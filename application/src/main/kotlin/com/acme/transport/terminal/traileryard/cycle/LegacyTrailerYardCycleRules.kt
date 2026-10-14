package com.acme.transport.terminal.traileryard.cycle

/** A rule a legacy trailer yard cycle must pass. Check returns its problems, or none. */
interface LegacyTrailerYardCycleRule {
    fun check(item: LegacyTrailerYardCycle): MutableList<String>
}

data class LegacyTrailerYardCycleRequiredCodeRule(var message: String) : LegacyTrailerYardCycleRule {
    override fun check(item: LegacyTrailerYardCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardCycleRequiredOwnerIdRule(var message: String) : LegacyTrailerYardCycleRule {
    override fun check(item: LegacyTrailerYardCycle): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardCycleSequenceRangeRule(var min: Int, var max: Int) : LegacyTrailerYardCycleRule {
    override fun check(item: LegacyTrailerYardCycle): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardCycleCodePrefixRule(var prefix: String) : LegacyTrailerYardCycleRule {
    override fun check(item: LegacyTrailerYardCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardCycleSourceLengthRule(var maxLength: Int) : LegacyTrailerYardCycleRule {
    override fun check(item: LegacyTrailerYardCycle): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy trailer yard cycle. */
data class LegacyTrailerYardCycleValidator(var rules: MutableList<LegacyTrailerYardCycleRule>) {
    fun validate(item: LegacyTrailerYardCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTrailerYardCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTrailerYardCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTrailerYardCycleValidator(): LegacyTrailerYardCycleValidator {
    return LegacyTrailerYardCycleValidator(
        mutableListOf<LegacyTrailerYardCycleRule>(
            LegacyTrailerYardCycleRequiredCodeRule("legacy trailer yard cycle needs a value"),
            LegacyTrailerYardCycleRequiredOwnerIdRule("legacy trailer yard cycle needs an entry"),
            LegacyTrailerYardCycleSequenceRangeRule(16, 86400),
            LegacyTrailerYardCycleCodePrefixRule("X"),
            LegacyTrailerYardCycleSourceLengthRule(86400),
        ),
    )
}
