package com.acme.transport.lastmile.locker.group

/** A rule a locker baseline must pass. Check returns its problems, or none. */
interface LockerBaselineRule {
    fun check(item: LockerBaseline): MutableList<String>
}

data class LockerBaselineCodePrefixRule(var prefix: String) : LockerBaselineRule {
    override fun check(item: LockerBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerBaselineRevisionRangeRule(var min: Int, var max: Int) : LockerBaselineRule {
    override fun check(item: LockerBaseline): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerBaselineRequiredCodeRule(var message: String) : LockerBaselineRule {
    override fun check(item: LockerBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker baseline. */
data class LockerBaselineValidator(var rules: MutableList<LockerBaselineRule>) {
    fun validate(item: LockerBaseline): MutableList<String> {
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

    fun requireValid(item: LockerBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerBaselineValidator(): LockerBaselineValidator {
    return LockerBaselineValidator(
        mutableListOf<LockerBaselineRule>(
            LockerBaselineCodePrefixRule("Q"),
            LockerBaselineRevisionRangeRule(16, 86400),
            LockerBaselineRequiredCodeRule("locker baseline needs a value"),
        ),
    )
}
