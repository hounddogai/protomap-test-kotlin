package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.normalize

/** A rule a locker rollup must pass. Check returns its problems, or none. */
interface LockerRollupRule {
    fun check(item: LockerRollup): MutableList<String>
}

data class LockerRollupNameLengthRule(var maxLength: Int) : LockerRollupRule {
    override fun check(item: LockerRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerRollupAllowedCodeRule(var allowed: MutableList<String>) : LockerRollupRule {
    override fun check(item: LockerRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LockerRollupTitleLengthRule(var maxLength: Int) : LockerRollupRule {
    override fun check(item: LockerRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerRollupCodePrefixRule(var prefix: String) : LockerRollupRule {
    override fun check(item: LockerRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerRollupCreatedAtRangeRule(var min: Int, var max: Int) : LockerRollupRule {
    override fun check(item: LockerRollup): MutableList<String> {
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

data class LockerRollupDockLengthRule(var maxLength: Int) : LockerRollupRule {
    override fun check(item: LockerRollup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker rollup. */
data class LockerRollupValidator(var rules: MutableList<LockerRollupRule>) {
    fun validate(item: LockerRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerRollupValidator(): LockerRollupValidator {
    return LockerRollupValidator(
        mutableListOf<LockerRollupRule>(
            LockerRollupNameLengthRule(1000),
            LockerRollupAllowedCodeRule(mutableListOf<String>("batched")),
            LockerRollupTitleLengthRule(3600),
            LockerRollupCodePrefixRule("Q"),
            LockerRollupCreatedAtRangeRule(120, 86400),
            LockerRollupDockLengthRule(3600),
        ),
    )
}
