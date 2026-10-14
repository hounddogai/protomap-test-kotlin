package com.acme.transport.fleetops.serviceplan.planrollup

/** A rule a tiered service plan rollup must pass. Check returns its problems, or none. */
interface TieredServicePlanRollupRule {
    fun check(item: TieredServicePlanRollup): MutableList<String>
}

data class TieredServicePlanRollupPalletsRangeRule(var min: Int, var max: Int) : TieredServicePlanRollupRule {
    override fun check(item: TieredServicePlanRollup): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanRollupTitleLengthRule(var maxLength: Int) : TieredServicePlanRollupRule {
    override fun check(item: TieredServicePlanRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanRollupActiveGuardRule(var limit: Int) : TieredServicePlanRollupRule {
    override fun check(item: TieredServicePlanRollup): MutableList<String> {
        if (item.active && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class TieredServicePlanRollupRequiredOwnerIdRule(var message: String) : TieredServicePlanRollupRule {
    override fun check(item: TieredServicePlanRollup): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered service plan rollup. */
data class TieredServicePlanRollupValidator(var rules: MutableList<TieredServicePlanRollupRule>) {
    fun validate(item: TieredServicePlanRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredServicePlanRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredServicePlanRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredServicePlanRollupValidator(): TieredServicePlanRollupValidator {
    return TieredServicePlanRollupValidator(
        mutableListOf<TieredServicePlanRollupRule>(
            TieredServicePlanRollupPalletsRangeRule(120, 255),
            TieredServicePlanRollupTitleLengthRule(120),
            TieredServicePlanRollupActiveGuardRule(3600),
            TieredServicePlanRollupRequiredOwnerIdRule("tiered service plan rollup needs this field"),
        ),
    )
}
