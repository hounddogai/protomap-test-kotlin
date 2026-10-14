package com.acme.transport.freightpay.fuelsurcharge.segment

/** A rule a lenient fuel surcharge window must pass. Check returns its problems, or none. */
interface LenientFuelSurchargeWindowRule {
    fun check(item: LenientFuelSurchargeWindow): MutableList<String>
}

data class LenientFuelSurchargeWindowCodePrefixRule(var prefix: String) : LenientFuelSurchargeWindowRule {
    override fun check(item: LenientFuelSurchargeWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFuelSurchargeWindowScoreRangeRule(
    var min: Double,
    var max: Double,
) : LenientFuelSurchargeWindowRule {
    override fun check(item: LenientFuelSurchargeWindow): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class LenientFuelSurchargeWindowCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LenientFuelSurchargeWindowRule {
    override fun check(item: LenientFuelSurchargeWindow): MutableList<String> {
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

data class LenientFuelSurchargeWindowLimitRangeRule(
    var min: Int,
    var max: Int,
) : LenientFuelSurchargeWindowRule {
    override fun check(item: LenientFuelSurchargeWindow): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient fuel surcharge window. */
data class LenientFuelSurchargeWindowValidator(var rules: MutableList<LenientFuelSurchargeWindowRule>) {
    fun validate(item: LenientFuelSurchargeWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientFuelSurchargeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFuelSurchargeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFuelSurchargeWindowValidator(): LenientFuelSurchargeWindowValidator {
    return LenientFuelSurchargeWindowValidator(
        mutableListOf<LenientFuelSurchargeWindowRule>(
            LenientFuelSurchargeWindowCodePrefixRule("ACM"),
            LenientFuelSurchargeWindowScoreRangeRule(0.8, 3333.5),
            LenientFuelSurchargeWindowCreatedAtRangeRule(32, 64),
            LenientFuelSurchargeWindowLimitRangeRule(80, 80),
        ),
    )
}
