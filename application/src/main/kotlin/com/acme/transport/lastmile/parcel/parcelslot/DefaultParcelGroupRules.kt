package com.acme.transport.lastmile.parcel.parcelslot

/** A rule a default parcel group must pass. Check returns its problems, or none. */
interface DefaultParcelGroupRule {
    fun check(item: DefaultParcelGroup): MutableList<String>
}

data class DefaultParcelGroupMarginRangeRule(var min: Double, var max: Double) : DefaultParcelGroupRule {
    override fun check(item: DefaultParcelGroup): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class DefaultParcelGroupRequiredCodeRule(var message: String) : DefaultParcelGroupRule {
    override fun check(item: DefaultParcelGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelGroupCodePrefixRule(var prefix: String) : DefaultParcelGroupRule {
    override fun check(item: DefaultParcelGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelGroupRequiredRegionRule(var message: String) : DefaultParcelGroupRule {
    override fun check(item: DefaultParcelGroup): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default parcel group. */
data class DefaultParcelGroupValidator(var rules: MutableList<DefaultParcelGroupRule>) {
    fun validate(item: DefaultParcelGroup): MutableList<String> {
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

    fun requireValid(item: DefaultParcelGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultParcelGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultParcelGroupValidator(): DefaultParcelGroupValidator {
    return DefaultParcelGroupValidator(
        mutableListOf<DefaultParcelGroupRule>(
            DefaultParcelGroupMarginRangeRule(7.5, 3478.4),
            DefaultParcelGroupRequiredCodeRule("default parcel group needs this field"),
            DefaultParcelGroupCodePrefixRule("ACM"),
            DefaultParcelGroupRequiredRegionRule("default parcel group needs a value"),
        ),
    )
}
