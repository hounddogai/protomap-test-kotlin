package com.acme.transport.network.zone.tier

/** A rule a zone tier must pass. Check returns its problems, or none. */
interface ZoneTierRule {
    fun check(item: ZoneTier): MutableList<String>
}

data class ZoneTierCodeLengthRule(var maxLength: Int) : ZoneTierRule {
    override fun check(item: ZoneTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneTierLabelLengthRule(var maxLength: Int) : ZoneTierRule {
    override fun check(item: ZoneTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneTierAttemptsRangeRule(var min: Int, var max: Int) : ZoneTierRule {
    override fun check(item: ZoneTier): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone tier. */
data class ZoneTierValidator(var rules: MutableList<ZoneTierRule>) {
    fun validate(item: ZoneTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneTier>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneTierValidator(): ZoneTierValidator {
    return ZoneTierValidator(
        mutableListOf<ZoneTierRule>(
            ZoneTierCodeLengthRule(16),
            ZoneTierLabelLengthRule(3600),
            ZoneTierAttemptsRangeRule(80, 80),
        ),
    )
}
