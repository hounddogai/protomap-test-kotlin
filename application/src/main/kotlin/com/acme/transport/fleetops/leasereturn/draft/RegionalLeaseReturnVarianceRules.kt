package com.acme.transport.fleetops.leasereturn.draft

/** A rule a regional lease return variance must pass. Check returns its problems, or none. */
interface RegionalLeaseReturnVarianceRule {
    fun check(item: RegionalLeaseReturnVariance): MutableList<String>
}

data class RegionalLeaseReturnVarianceRequiredTitleRule(var message: String) : RegionalLeaseReturnVarianceRule {
    override fun check(item: RegionalLeaseReturnVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnVarianceRequiredCodeRule(var message: String) : RegionalLeaseReturnVarianceRule {
    override fun check(item: RegionalLeaseReturnVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnVarianceCostRangeRule(
    var min: Double,
    var max: Double,
) : RegionalLeaseReturnVarianceRule {
    override fun check(item: RegionalLeaseReturnVariance): MutableList<String> {
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

data class RegionalLeaseReturnVarianceCodePrefixRule(var prefix: String) : RegionalLeaseReturnVarianceRule {
    override fun check(item: RegionalLeaseReturnVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalLeaseReturnVarianceRequiredDepotRule(var message: String) : RegionalLeaseReturnVarianceRule {
    override fun check(item: RegionalLeaseReturnVariance): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional lease return variance. */
data class RegionalLeaseReturnVarianceValidator(var rules: MutableList<RegionalLeaseReturnVarianceRule>) {
    fun validate(item: RegionalLeaseReturnVariance): MutableList<String> {
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

    fun requireValid(item: RegionalLeaseReturnVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalLeaseReturnVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalLeaseReturnVarianceValidator(): RegionalLeaseReturnVarianceValidator {
    return RegionalLeaseReturnVarianceValidator(
        mutableListOf<RegionalLeaseReturnVarianceRule>(
            RegionalLeaseReturnVarianceRequiredTitleRule("regional lease return variance needs this field"),
            RegionalLeaseReturnVarianceRequiredCodeRule("regional lease return variance needs this field"),
            RegionalLeaseReturnVarianceCostRangeRule(8.4, 2564.7),
            RegionalLeaseReturnVarianceCodePrefixRule("ACM"),
            RegionalLeaseReturnVarianceRequiredDepotRule("regional lease return variance needs this field"),
        ),
    )
}
