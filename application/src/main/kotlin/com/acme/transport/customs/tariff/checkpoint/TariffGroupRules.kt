package com.acme.transport.customs.tariff.checkpoint

/** A rule a tariff group must pass. Check returns its problems, or none. */
interface TariffGroupRule {
    fun check(item: TariffGroup): MutableList<String>
}

data class TariffGroupCodeLengthRule(var maxLength: Int) : TariffGroupRule {
    override fun check(item: TariffGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffGroupNameLengthRule(var maxLength: Int) : TariffGroupRule {
    override fun check(item: TariffGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffGroupRequiredRouteCodeRule(var message: String) : TariffGroupRule {
    override fun check(item: TariffGroup): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffGroupCostRangeRule(var min: Double, var max: Double) : TariffGroupRule {
    override fun check(item: TariffGroup): MutableList<String> {
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

data class TariffGroupRequiredSegmentRule(var message: String) : TariffGroupRule {
    override fun check(item: TariffGroup): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff group. */
data class TariffGroupValidator(var rules: MutableList<TariffGroupRule>) {
    fun validate(item: TariffGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffGroupValidator(): TariffGroupValidator {
    return TariffGroupValidator(
        mutableListOf<TariffGroupRule>(
            TariffGroupCodeLengthRule(255),
            TariffGroupNameLengthRule(16),
            TariffGroupRequiredRouteCodeRule("tariff group needs this field"),
            TariffGroupCostRangeRule(8.0, 1233.6),
            TariffGroupRequiredSegmentRule("tariff group needs an entry"),
        ),
    )
}
