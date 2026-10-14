package com.acme.transport.fleetops.serviceplan.agreement

/** A rule a capped service plan rollup must pass. Check returns its problems, or none. */
interface CappedServicePlanRollupRule {
    fun check(item: CappedServicePlanRollup): MutableList<String>
}

data class CappedServicePlanRollupCodeLengthRule(var maxLength: Int) : CappedServicePlanRollupRule {
    override fun check(item: CappedServicePlanRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedServicePlanRollupRetriesRangeRule(var min: Int, var max: Int) : CappedServicePlanRollupRule {
    override fun check(item: CappedServicePlanRollup): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedServicePlanRollupFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : CappedServicePlanRollupRule {
    override fun check(item: CappedServicePlanRollup): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class CappedServicePlanRollupRequiredVehicleIdRule(var message: String) : CappedServicePlanRollupRule {
    override fun check(item: CappedServicePlanRollup): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped service plan rollup. */
data class CappedServicePlanRollupValidator(var rules: MutableList<CappedServicePlanRollupRule>) {
    fun validate(item: CappedServicePlanRollup): MutableList<String> {
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

    fun requireValid(item: CappedServicePlanRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedServicePlanRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedServicePlanRollupValidator(): CappedServicePlanRollupValidator {
    return CappedServicePlanRollupValidator(
        mutableListOf<CappedServicePlanRollupRule>(
            CappedServicePlanRollupCodeLengthRule(86400),
            CappedServicePlanRollupRetriesRangeRule(64, 86400),
            CappedServicePlanRollupFloorValueRangeRule(5.4, 1514.4),
            CappedServicePlanRollupRequiredVehicleIdRule("capped service plan rollup needs an entry"),
        ),
    )
}
