package com.acme.transport.crewing.roster.entry

/** A rule a tiered roster exception must pass. Check returns its problems, or none. */
interface TieredRosterExceptionRule {
    fun check(item: TieredRosterException): MutableList<String>
}

data class TieredRosterExceptionRequiredCodeRule(var message: String) : TieredRosterExceptionRule {
    override fun check(item: TieredRosterException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterExceptionRequiredCarrierRule(var message: String) : TieredRosterExceptionRule {
    override fun check(item: TieredRosterException): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterExceptionCodeLengthRule(var maxLength: Int) : TieredRosterExceptionRule {
    override fun check(item: TieredRosterException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterExceptionCodePrefixRule(var prefix: String) : TieredRosterExceptionRule {
    override fun check(item: TieredRosterException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRosterExceptionCostRangeRule(var min: Double, var max: Double) : TieredRosterExceptionRule {
    override fun check(item: TieredRosterException): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered roster exception. */
data class TieredRosterExceptionValidator(var rules: MutableList<TieredRosterExceptionRule>) {
    fun validate(item: TieredRosterException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRosterException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRosterException>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRosterExceptionValidator(): TieredRosterExceptionValidator {
    return TieredRosterExceptionValidator(
        mutableListOf<TieredRosterExceptionRule>(
            TieredRosterExceptionRequiredCodeRule("tiered roster exception needs an entry"),
            TieredRosterExceptionRequiredCarrierRule("tiered roster exception needs an entry"),
            TieredRosterExceptionCodeLengthRule(1000),
            TieredRosterExceptionCodePrefixRule("X"),
            TieredRosterExceptionCostRangeRule(4.0, 2551.2),
        ),
    )
}
