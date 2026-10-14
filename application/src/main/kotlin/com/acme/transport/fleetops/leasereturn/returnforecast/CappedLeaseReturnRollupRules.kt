package com.acme.transport.fleetops.leasereturn.returnforecast

/** A rule a capped lease return rollup must pass. Check returns its problems, or none. */
interface CappedLeaseReturnRollupRule {
    fun check(item: CappedLeaseReturnRollup): MutableList<String>
}

data class CappedLeaseReturnRollupRateRangeRule(
    var min: Double,
    var max: Double,
) : CappedLeaseReturnRollupRule {
    override fun check(item: CappedLeaseReturnRollup): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedLeaseReturnRollupCodeLengthRule(var maxLength: Int) : CappedLeaseReturnRollupRule {
    override fun check(item: CappedLeaseReturnRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLeaseReturnRollupLabelLengthRule(var maxLength: Int) : CappedLeaseReturnRollupRule {
    override fun check(item: CappedLeaseReturnRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLeaseReturnRollupCodePrefixRule(var prefix: String) : CappedLeaseReturnRollupRule {
    override fun check(item: CappedLeaseReturnRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedLeaseReturnRollupDiscountRangeRule(
    var min: Double,
    var max: Double,
) : CappedLeaseReturnRollupRule {
    override fun check(item: CappedLeaseReturnRollup): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped lease return rollup. */
data class CappedLeaseReturnRollupValidator(var rules: MutableList<CappedLeaseReturnRollupRule>) {
    fun validate(item: CappedLeaseReturnRollup): MutableList<String> {
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

    fun requireValid(item: CappedLeaseReturnRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLeaseReturnRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLeaseReturnRollupValidator(): CappedLeaseReturnRollupValidator {
    return CappedLeaseReturnRollupValidator(
        mutableListOf<CappedLeaseReturnRollupRule>(
            CappedLeaseReturnRollupRateRangeRule(10.0, 4147.4),
            CappedLeaseReturnRollupCodeLengthRule(86400),
            CappedLeaseReturnRollupLabelLengthRule(64),
            CappedLeaseReturnRollupCodePrefixRule("ACM"),
            CappedLeaseReturnRollupDiscountRangeRule(5.1, 3965.1),
        ),
    )
}
