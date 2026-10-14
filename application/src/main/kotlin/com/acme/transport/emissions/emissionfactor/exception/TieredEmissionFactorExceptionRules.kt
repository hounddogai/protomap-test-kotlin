package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.text.normalize

/** A rule a tiered emission factor exception must pass. Check returns its problems, or none. */
interface TieredEmissionFactorExceptionRule {
    fun check(item: TieredEmissionFactorException): MutableList<String>
}

data class TieredEmissionFactorExceptionAllowedTitleRule(
    var allowed: MutableList<String>,
) : TieredEmissionFactorExceptionRule {
    override fun check(item: TieredEmissionFactorException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorExceptionOdometerRangeRule(
    var min: Int,
    var max: Int,
) : TieredEmissionFactorExceptionRule {
    override fun check(item: TieredEmissionFactorException): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class TieredEmissionFactorExceptionRequiredTitleRule(
    var message: String,
) : TieredEmissionFactorExceptionRule {
    override fun check(item: TieredEmissionFactorException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredEmissionFactorExceptionCodePrefixRule(var prefix: String) : TieredEmissionFactorExceptionRule {
    override fun check(item: TieredEmissionFactorException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered emission factor exception. */
data class TieredEmissionFactorExceptionValidator(var rules: MutableList<TieredEmissionFactorExceptionRule>) {
    fun validate(item: TieredEmissionFactorException): MutableList<String> {
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

    fun requireValid(item: TieredEmissionFactorException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredEmissionFactorException>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredEmissionFactorExceptionValidator(): TieredEmissionFactorExceptionValidator {
    return TieredEmissionFactorExceptionValidator(
        mutableListOf<TieredEmissionFactorExceptionRule>(
            TieredEmissionFactorExceptionAllowedTitleRule(mutableListOf<String>("adaptive", "batched")),
            TieredEmissionFactorExceptionOdometerRangeRule(1000, 3600),
            TieredEmissionFactorExceptionRequiredTitleRule("tiered emission factor exception needs this field"),
            TieredEmissionFactorExceptionCodePrefixRule("ACM"),
        ),
    )
}
