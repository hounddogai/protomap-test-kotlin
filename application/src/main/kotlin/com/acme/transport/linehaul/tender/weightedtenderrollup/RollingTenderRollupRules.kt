package com.acme.transport.linehaul.tender.weightedtenderrollup

/** A rule a rolling tender rollup must pass. Check returns its problems, or none. */
interface RollingTenderRollupRule {
    fun check(item: RollingTenderRollup): MutableList<String>
}

data class RollingTenderRollupExpiresAtRangeRule(var min: Int, var max: Int) : RollingTenderRollupRule {
    override fun check(item: RollingTenderRollup): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderRollupRevisionRangeRule(var min: Int, var max: Int) : RollingTenderRollupRule {
    override fun check(item: RollingTenderRollup): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class RollingTenderRollupDockLengthRule(var maxLength: Int) : RollingTenderRollupRule {
    override fun check(item: RollingTenderRollup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderRollupPhoneLengthRule(var maxLength: Int) : RollingTenderRollupRule {
    override fun check(item: RollingTenderRollup): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderRollupCodeLengthRule(var maxLength: Int) : RollingTenderRollupRule {
    override fun check(item: RollingTenderRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderRollupRequiredTitleRule(var message: String) : RollingTenderRollupRule {
    override fun check(item: RollingTenderRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tender rollup. */
data class RollingTenderRollupValidator(var rules: MutableList<RollingTenderRollupRule>) {
    fun validate(item: RollingTenderRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingTenderRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTenderRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTenderRollupValidator(): RollingTenderRollupValidator {
    return RollingTenderRollupValidator(
        mutableListOf<RollingTenderRollupRule>(
            RollingTenderRollupExpiresAtRangeRule(16, 16),
            RollingTenderRollupRevisionRangeRule(3600, 86400),
            RollingTenderRollupDockLengthRule(120),
            RollingTenderRollupPhoneLengthRule(16),
            RollingTenderRollupCodeLengthRule(32),
            RollingTenderRollupRequiredTitleRule("rolling tender rollup needs a value"),
        ),
    )
}
