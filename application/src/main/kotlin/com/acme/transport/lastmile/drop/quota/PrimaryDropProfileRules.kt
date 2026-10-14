package com.acme.transport.lastmile.drop.quota

/** A rule a primary drop profile must pass. Check returns its problems, or none. */
interface PrimaryDropProfileRule {
    fun check(item: PrimaryDropProfile): MutableList<String>
}

data class PrimaryDropProfileRequiredDepotRule(var message: String) : PrimaryDropProfileRule {
    override fun check(item: PrimaryDropProfile): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropProfileRequiredNameRule(var message: String) : PrimaryDropProfileRule {
    override fun check(item: PrimaryDropProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropProfileRouteCodeLengthRule(var maxLength: Int) : PrimaryDropProfileRule {
    override fun check(item: PrimaryDropProfile): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropProfileCostRangeRule(var min: Double, var max: Double) : PrimaryDropProfileRule {
    override fun check(item: PrimaryDropProfile): MutableList<String> {
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

/** Runs every rule of a primary drop profile. */
data class PrimaryDropProfileValidator(var rules: MutableList<PrimaryDropProfileRule>) {
    fun validate(item: PrimaryDropProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDropProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDropProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDropProfileValidator(): PrimaryDropProfileValidator {
    return PrimaryDropProfileValidator(
        mutableListOf<PrimaryDropProfileRule>(
            PrimaryDropProfileRequiredDepotRule("primary drop profile needs a value"),
            PrimaryDropProfileRequiredNameRule("primary drop profile needs an entry"),
            PrimaryDropProfileRouteCodeLengthRule(16),
            PrimaryDropProfileCostRangeRule(0.8, 1224.5),
        ),
    )
}
