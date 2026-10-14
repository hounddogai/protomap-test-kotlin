package com.acme.transport.lastmile.drop.rollup

/** A rule a drop rollup must pass. Check returns its problems, or none. */
interface DropRollupRule {
    fun check(item: DropRollup): MutableList<String>
}

data class DropRollupStartsAtRangeRule(var min: Int, var max: Int) : DropRollupRule {
    override fun check(item: DropRollup): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropRollupCodeLengthRule(var maxLength: Int) : DropRollupRule {
    override fun check(item: DropRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropRollupNameLengthRule(var maxLength: Int) : DropRollupRule {
    override fun check(item: DropRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop rollup. */
data class DropRollupValidator(var rules: MutableList<DropRollupRule>) {
    fun validate(item: DropRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultDropRollupValidator(): DropRollupValidator {
    return DropRollupValidator(
        mutableListOf<DropRollupRule>(
            DropRollupStartsAtRangeRule(3600, 86400),
            DropRollupCodeLengthRule(86400),
            DropRollupNameLengthRule(32),
        ),
    )
}
