package com.acme.transport.lastmile.parcel.parcelcycle

/** A rule a lenient parcel rollup must pass. Check returns its problems, or none. */
interface LenientParcelRollupRule {
    fun check(item: LenientParcelRollup): MutableList<String>
}

data class LenientParcelRollupNameLengthRule(var maxLength: Int) : LenientParcelRollupRule {
    override fun check(item: LenientParcelRollup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelRollupAmountRangeRule(var min: Double, var max: Double) : LenientParcelRollupRule {
    override fun check(item: LenientParcelRollup): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelRollupStartsAtRangeRule(var min: Int, var max: Int) : LenientParcelRollupRule {
    override fun check(item: LenientParcelRollup): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class LenientParcelRollupCodePrefixRule(var prefix: String) : LenientParcelRollupRule {
    override fun check(item: LenientParcelRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient parcel rollup. */
data class LenientParcelRollupValidator(var rules: MutableList<LenientParcelRollupRule>) {
    fun validate(item: LenientParcelRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientParcelRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientParcelRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientParcelRollupValidator(): LenientParcelRollupValidator {
    return LenientParcelRollupValidator(
        mutableListOf<LenientParcelRollupRule>(
            LenientParcelRollupNameLengthRule(120),
            LenientParcelRollupAmountRangeRule(9.9, 3280.9),
            LenientParcelRollupStartsAtRangeRule(16, 255),
            LenientParcelRollupCodePrefixRule("Q"),
        ),
    )
}
