package com.acme.transport.fleetops.utilization.target

/** A rule a utilization mapping must pass. Check returns its problems, or none. */
interface UtilizationMappingRule {
    fun check(item: UtilizationMapping): MutableList<String>
}

data class UtilizationMappingRequiredChannelRule(var message: String) : UtilizationMappingRule {
    override fun check(item: UtilizationMapping): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMappingRequiredShiftCodeRule(var message: String) : UtilizationMappingRule {
    override fun check(item: UtilizationMapping): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationMappingRequiredCodeRule(var message: String) : UtilizationMappingRule {
    override fun check(item: UtilizationMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization mapping. */
data class UtilizationMappingValidator(var rules: MutableList<UtilizationMappingRule>) {
    fun validate(item: UtilizationMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationMappingValidator(): UtilizationMappingValidator {
    return UtilizationMappingValidator(
        mutableListOf<UtilizationMappingRule>(
            UtilizationMappingRequiredChannelRule("utilization mapping needs this field"),
            UtilizationMappingRequiredShiftCodeRule("utilization mapping needs this field"),
            UtilizationMappingRequiredCodeRule("utilization mapping needs this field"),
        ),
    )
}
