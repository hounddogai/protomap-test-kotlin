package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.text.normalize

/** A rule a default near miss ledger must pass. Check returns its problems, or none. */
interface DefaultNearMissLedgerRule {
    fun check(item: DefaultNearMissLedger): MutableList<String>
}

data class DefaultNearMissLedgerDriverIdLengthRule(var maxLength: Int) : DefaultNearMissLedgerRule {
    override fun check(item: DefaultNearMissLedger): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissLedgerTitleLengthRule(var maxLength: Int) : DefaultNearMissLedgerRule {
    override fun check(item: DefaultNearMissLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissLedgerAttemptsRangeRule(var min: Int, var max: Int) : DefaultNearMissLedgerRule {
    override fun check(item: DefaultNearMissLedger): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class DefaultNearMissLedgerAllowedCodeRule(var allowed: MutableList<String>) : DefaultNearMissLedgerRule {
    override fun check(item: DefaultNearMissLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultNearMissLedgerThresholdRangeRule(
    var min: Double,
    var max: Double,
) : DefaultNearMissLedgerRule {
    override fun check(item: DefaultNearMissLedger): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class DefaultNearMissLedgerCodePrefixRule(var prefix: String) : DefaultNearMissLedgerRule {
    override fun check(item: DefaultNearMissLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default near miss ledger. */
data class DefaultNearMissLedgerValidator(var rules: MutableList<DefaultNearMissLedgerRule>) {
    fun validate(item: DefaultNearMissLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultNearMissLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultNearMissLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultNearMissLedgerValidator(): DefaultNearMissLedgerValidator {
    return DefaultNearMissLedgerValidator(
        mutableListOf<DefaultNearMissLedgerRule>(
            DefaultNearMissLedgerDriverIdLengthRule(1000),
            DefaultNearMissLedgerTitleLengthRule(86400),
            DefaultNearMissLedgerAttemptsRangeRule(64, 1000),
            DefaultNearMissLedgerAllowedCodeRule(mutableListOf<String>("scheduled", "lenient")),
            DefaultNearMissLedgerThresholdRangeRule(5.0, 468.0),
            DefaultNearMissLedgerCodePrefixRule("REF"),
        ),
    )
}
