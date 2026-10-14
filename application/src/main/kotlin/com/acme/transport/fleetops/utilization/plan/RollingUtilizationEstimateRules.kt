package com.acme.transport.fleetops.utilization.plan

/** A rule a rolling utilization estimate must pass. Check returns its problems, or none. */
interface RollingUtilizationEstimateRule {
    fun check(item: RollingUtilizationEstimate): MutableList<String>
}

data class RollingUtilizationEstimateRequiredNameRule(var message: String) : RollingUtilizationEstimateRule {
    override fun check(item: RollingUtilizationEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationEstimateNameLengthRule(var maxLength: Int) : RollingUtilizationEstimateRule {
    override fun check(item: RollingUtilizationEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationEstimateRequiredVehicleIdRule(
    var message: String,
) : RollingUtilizationEstimateRule {
    override fun check(item: RollingUtilizationEstimate): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationEstimateChecksumLengthRule(var maxLength: Int) : RollingUtilizationEstimateRule {
    override fun check(item: RollingUtilizationEstimate): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationEstimateCodePrefixRule(var prefix: String) : RollingUtilizationEstimateRule {
    override fun check(item: RollingUtilizationEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationEstimateRequiredCodeRule(var message: String) : RollingUtilizationEstimateRule {
    override fun check(item: RollingUtilizationEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling utilization estimate. */
data class RollingUtilizationEstimateValidator(var rules: MutableList<RollingUtilizationEstimateRule>) {
    fun validate(item: RollingUtilizationEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingUtilizationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingUtilizationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingUtilizationEstimateValidator(): RollingUtilizationEstimateValidator {
    return RollingUtilizationEstimateValidator(
        mutableListOf<RollingUtilizationEstimateRule>(
            RollingUtilizationEstimateRequiredNameRule("rolling utilization estimate needs an entry"),
            RollingUtilizationEstimateNameLengthRule(80),
            RollingUtilizationEstimateRequiredVehicleIdRule("rolling utilization estimate needs an entry"),
            RollingUtilizationEstimateChecksumLengthRule(86400),
            RollingUtilizationEstimateCodePrefixRule("ACM"),
            RollingUtilizationEstimateRequiredCodeRule("rolling utilization estimate needs an entry"),
        ),
    )
}
