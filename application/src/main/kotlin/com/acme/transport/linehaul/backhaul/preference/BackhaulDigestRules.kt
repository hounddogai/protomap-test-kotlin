package com.acme.transport.linehaul.backhaul.preference

/** A rule a backhaul digest must pass. Check returns its problems, or none. */
interface BackhaulDigestRule {
    fun check(item: BackhaulDigest): MutableList<String>
}

data class BackhaulDigestCodeLengthRule(var maxLength: Int) : BackhaulDigestRule {
    override fun check(item: BackhaulDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulDigestCodePrefixRule(var prefix: String) : BackhaulDigestRule {
    override fun check(item: BackhaulDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulDigestCostRangeRule(var min: Double, var max: Double) : BackhaulDigestRule {
    override fun check(item: BackhaulDigest): MutableList<String> {
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

data class BackhaulDigestCurrencyLengthRule(var maxLength: Int) : BackhaulDigestRule {
    override fun check(item: BackhaulDigest): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul digest. */
data class BackhaulDigestValidator(var rules: MutableList<BackhaulDigestRule>) {
    fun validate(item: BackhaulDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulDigestValidator(): BackhaulDigestValidator {
    return BackhaulDigestValidator(
        mutableListOf<BackhaulDigestRule>(
            BackhaulDigestCodeLengthRule(1000),
            BackhaulDigestCodePrefixRule("REF"),
            BackhaulDigestCostRangeRule(5.8, 4795.6),
            BackhaulDigestCurrencyLengthRule(16),
        ),
    )
}
