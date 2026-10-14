package com.acme.transport.network.hub.plan

/** A rule a tiered hub baseline must pass. Check returns its problems, or none. */
interface TieredHubBaselineRule {
    fun check(item: TieredHubBaseline): MutableList<String>
}

data class TieredHubBaselineRequiredVehicleIdRule(var message: String) : TieredHubBaselineRule {
    override fun check(item: TieredHubBaseline): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredHubBaselineRequiredNameRule(var message: String) : TieredHubBaselineRule {
    override fun check(item: TieredHubBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredHubBaselineLimitRangeRule(var min: Int, var max: Int) : TieredHubBaselineRule {
    override fun check(item: TieredHubBaseline): MutableList<String> {
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

data class TieredHubBaselinePinnedGuardRule(var limit: Int) : TieredHubBaselineRule {
    override fun check(item: TieredHubBaseline): MutableList<String> {
        if (item.pinned && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered hub baseline. */
data class TieredHubBaselineValidator(var rules: MutableList<TieredHubBaselineRule>) {
    fun validate(item: TieredHubBaseline): MutableList<String> {
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

    fun requireValid(item: TieredHubBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredHubBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredHubBaselineValidator(): TieredHubBaselineValidator {
    return TieredHubBaselineValidator(
        mutableListOf<TieredHubBaselineRule>(
            TieredHubBaselineRequiredVehicleIdRule("tiered hub baseline needs a value"),
            TieredHubBaselineRequiredNameRule("tiered hub baseline needs this field"),
            TieredHubBaselineLimitRangeRule(1000, 86400),
            TieredHubBaselinePinnedGuardRule(86400),
        ),
    )
}
