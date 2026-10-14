package com.acme.transport.network.zone.batchedzonemapping

/** A rule a rolling zone mapping must pass. Check returns its problems, or none. */
interface RollingZoneMappingRule {
    fun check(item: RollingZoneMapping): MutableList<String>
}

data class RollingZoneMappingArchivedGuardRule(var limit: Int) : RollingZoneMappingRule {
    override fun check(item: RollingZoneMapping): MutableList<String> {
        if (item.archived && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class RollingZoneMappingCapacityRangeRule(var min: Int, var max: Int) : RollingZoneMappingRule {
    override fun check(item: RollingZoneMapping): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class RollingZoneMappingMaximumRangeRule(var min: Double, var max: Double) : RollingZoneMappingRule {
    override fun check(item: RollingZoneMapping): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class RollingZoneMappingBatchSizeRangeRule(var min: Int, var max: Int) : RollingZoneMappingRule {
    override fun check(item: RollingZoneMapping): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling zone mapping. */
data class RollingZoneMappingValidator(var rules: MutableList<RollingZoneMappingRule>) {
    fun validate(item: RollingZoneMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingZoneMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingZoneMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingZoneMappingValidator(): RollingZoneMappingValidator {
    return RollingZoneMappingValidator(
        mutableListOf<RollingZoneMappingRule>(
            RollingZoneMappingArchivedGuardRule(120),
            RollingZoneMappingCapacityRangeRule(16, 120),
            RollingZoneMappingMaximumRangeRule(8.2, 4189.2),
            RollingZoneMappingBatchSizeRangeRule(1000, 3600),
        ),
    )
}
