package com.acme.transport.fleetops.serviceplan.rollup

/** A rule a service plan rollup must pass. Check returns its problems, or none. */
interface ServicePlanRollupRule {
    fun check(item: ServicePlanRollup): MutableList<String>
}

data class ServicePlanRollupRequiredShiftCodeRule(var message: String) : ServicePlanRollupRule {
    override fun check(item: ServicePlanRollup): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRollupSequenceRangeRule(var min: Int, var max: Int) : ServicePlanRollupRule {
    override fun check(item: ServicePlanRollup): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanRollupThresholdRangeRule(var min: Double, var max: Double) : ServicePlanRollupRule {
    override fun check(item: ServicePlanRollup): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan rollup. */
data class ServicePlanRollupValidator(var rules: MutableList<ServicePlanRollupRule>) {
    fun validate(item: ServicePlanRollup): MutableList<String> {
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

    fun requireValid(item: ServicePlanRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanRollupValidator(): ServicePlanRollupValidator {
    return ServicePlanRollupValidator(
        mutableListOf<ServicePlanRollupRule>(
            ServicePlanRollupRequiredShiftCodeRule("service plan rollup needs this field"),
            ServicePlanRollupSequenceRangeRule(80, 86400),
            ServicePlanRollupThresholdRangeRule(5.7, 834.0),
        ),
    )
}
