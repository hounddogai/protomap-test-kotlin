package com.acme.transport.fleetops.utilization.group

/** A rule a tiered utilization group must pass. Check returns its problems, or none. */
interface TieredUtilizationGroupRule {
    fun check(item: TieredUtilizationGroup): MutableList<String>
}

data class TieredUtilizationGroupCodePrefixRule(var prefix: String) : TieredUtilizationGroupRule {
    override fun check(item: TieredUtilizationGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredUtilizationGroupLabelLengthRule(var maxLength: Int) : TieredUtilizationGroupRule {
    override fun check(item: TieredUtilizationGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredUtilizationGroupCodeLengthRule(var maxLength: Int) : TieredUtilizationGroupRule {
    override fun check(item: TieredUtilizationGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredUtilizationGroupRequiredRouteCodeRule(var message: String) : TieredUtilizationGroupRule {
    override fun check(item: TieredUtilizationGroup): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredUtilizationGroupRequiredCodeRule(var message: String) : TieredUtilizationGroupRule {
    override fun check(item: TieredUtilizationGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered utilization group. */
data class TieredUtilizationGroupValidator(var rules: MutableList<TieredUtilizationGroupRule>) {
    fun validate(item: TieredUtilizationGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredUtilizationGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredUtilizationGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredUtilizationGroupValidator(): TieredUtilizationGroupValidator {
    return TieredUtilizationGroupValidator(
        mutableListOf<TieredUtilizationGroupRule>(
            TieredUtilizationGroupCodePrefixRule("OPS"),
            TieredUtilizationGroupLabelLengthRule(120),
            TieredUtilizationGroupCodeLengthRule(1000),
            TieredUtilizationGroupRequiredRouteCodeRule("tiered utilization group needs this field"),
            TieredUtilizationGroupRequiredCodeRule("tiered utilization group needs an entry"),
        ),
    )
}
