package com.acme.transport.fleetops.serviceplan.planrollup

/** A rule a service plan digest must pass. Check returns its problems, or none. */
interface ServicePlanDigestRule {
    fun check(item: ServicePlanDigest): MutableList<String>
}

data class ServicePlanDigestCodeLengthRule(var maxLength: Int) : ServicePlanDigestRule {
    override fun check(item: ServicePlanDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanDigestCeilingRangeRule(var min: Double, var max: Double) : ServicePlanDigestRule {
    override fun check(item: ServicePlanDigest): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanDigestNameLengthRule(var maxLength: Int) : ServicePlanDigestRule {
    override fun check(item: ServicePlanDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanDigestChecksumLengthRule(var maxLength: Int) : ServicePlanDigestRule {
    override fun check(item: ServicePlanDigest): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanDigestCodePrefixRule(var prefix: String) : ServicePlanDigestRule {
    override fun check(item: ServicePlanDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanDigestRequiredCodeRule(var message: String) : ServicePlanDigestRule {
    override fun check(item: ServicePlanDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan digest. */
data class ServicePlanDigestValidator(var rules: MutableList<ServicePlanDigestRule>) {
    fun validate(item: ServicePlanDigest): MutableList<String> {
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

    fun requireValid(item: ServicePlanDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanDigestValidator(): ServicePlanDigestValidator {
    return ServicePlanDigestValidator(
        mutableListOf<ServicePlanDigestRule>(
            ServicePlanDigestCodeLengthRule(255),
            ServicePlanDigestCeilingRangeRule(9.9, 3034.0),
            ServicePlanDigestNameLengthRule(32),
            ServicePlanDigestChecksumLengthRule(1000),
            ServicePlanDigestCodePrefixRule("ACM"),
            ServicePlanDigestRequiredCodeRule("service plan digest needs a value"),
        ),
    )
}
