package com.acme.transport.lastmile.locker.manifest

/** A rule a rolling locker policy must pass. Check returns its problems, or none. */
interface RollingLockerPolicyRule {
    fun check(item: RollingLockerPolicy): MutableList<String>
}

data class RollingLockerPolicyRequiredDockRule(var message: String) : RollingLockerPolicyRule {
    override fun check(item: RollingLockerPolicy): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLockerPolicyRequiredTitleRule(var message: String) : RollingLockerPolicyRule {
    override fun check(item: RollingLockerPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLockerPolicyCodeLengthRule(var maxLength: Int) : RollingLockerPolicyRule {
    override fun check(item: RollingLockerPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingLockerPolicyRequiredCodeRule(var message: String) : RollingLockerPolicyRule {
    override fun check(item: RollingLockerPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLockerPolicyPalletsRangeRule(var min: Int, var max: Int) : RollingLockerPolicyRule {
    override fun check(item: RollingLockerPolicy): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling locker policy. */
data class RollingLockerPolicyValidator(var rules: MutableList<RollingLockerPolicyRule>) {
    fun validate(item: RollingLockerPolicy): MutableList<String> {
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

    fun requireValid(item: RollingLockerPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingLockerPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingLockerPolicyValidator(): RollingLockerPolicyValidator {
    return RollingLockerPolicyValidator(
        mutableListOf<RollingLockerPolicyRule>(
            RollingLockerPolicyRequiredDockRule("rolling locker policy needs an entry"),
            RollingLockerPolicyRequiredTitleRule("rolling locker policy needs a value"),
            RollingLockerPolicyCodeLengthRule(32),
            RollingLockerPolicyRequiredCodeRule("rolling locker policy needs this field"),
            RollingLockerPolicyPalletsRangeRule(16, 80),
        ),
    )
}
