package com.acme.transport.lastmile.parcel.claim

/** A rule a regional parcel variance must pass. Check returns its problems, or none. */
interface RegionalParcelVarianceRule {
    fun check(item: RegionalParcelVariance): MutableList<String>
}

data class RegionalParcelVarianceRequiredCodeRule(var message: String) : RegionalParcelVarianceRule {
    override fun check(item: RegionalParcelVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelVarianceRequiredDepotRule(var message: String) : RegionalParcelVarianceRule {
    override fun check(item: RegionalParcelVariance): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelVarianceCodePrefixRule(var prefix: String) : RegionalParcelVarianceRule {
    override fun check(item: RegionalParcelVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelVarianceRequiredNameRule(var message: String) : RegionalParcelVarianceRule {
    override fun check(item: RegionalParcelVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelVarianceNameLengthRule(var maxLength: Int) : RegionalParcelVarianceRule {
    override fun check(item: RegionalParcelVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelVarianceMinimumRangeRule(
    var min: Double,
    var max: Double,
) : RegionalParcelVarianceRule {
    override fun check(item: RegionalParcelVariance): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional parcel variance. */
data class RegionalParcelVarianceValidator(var rules: MutableList<RegionalParcelVarianceRule>) {
    fun validate(item: RegionalParcelVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalParcelVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalParcelVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalParcelVarianceValidator(): RegionalParcelVarianceValidator {
    return RegionalParcelVarianceValidator(
        mutableListOf<RegionalParcelVarianceRule>(
            RegionalParcelVarianceRequiredCodeRule("regional parcel variance needs a value"),
            RegionalParcelVarianceRequiredDepotRule("regional parcel variance needs an entry"),
            RegionalParcelVarianceCodePrefixRule("Q"),
            RegionalParcelVarianceRequiredNameRule("regional parcel variance needs a value"),
            RegionalParcelVarianceNameLengthRule(120),
            RegionalParcelVarianceMinimumRangeRule(6.1, 2258.1),
        ),
    )
}
