package com.acme.transport.fleetops.serviceplan.agreement

/** A rule a lenient service plan digest must pass. Check returns its problems, or none. */
interface LenientServicePlanDigestRule {
    fun check(item: LenientServicePlanDigest): MutableList<String>
}

data class LenientServicePlanDigestVehicleIdLengthRule(var maxLength: Int) : LenientServicePlanDigestRule {
    override fun check(item: LenientServicePlanDigest): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientServicePlanDigestAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : LenientServicePlanDigestRule {
    override fun check(item: LenientServicePlanDigest): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class LenientServicePlanDigestRequiredCodeRule(var message: String) : LenientServicePlanDigestRule {
    override fun check(item: LenientServicePlanDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServicePlanDigestApprovedGuardRule(var limit: Int) : LenientServicePlanDigestRule {
    override fun check(item: LenientServicePlanDigest): MutableList<String> {
        if (item.approved && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LenientServicePlanDigestCodeLengthRule(var maxLength: Int) : LenientServicePlanDigestRule {
    override fun check(item: LenientServicePlanDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient service plan digest. */
data class LenientServicePlanDigestValidator(var rules: MutableList<LenientServicePlanDigestRule>) {
    fun validate(item: LenientServicePlanDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientServicePlanDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServicePlanDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServicePlanDigestValidator(): LenientServicePlanDigestValidator {
    return LenientServicePlanDigestValidator(
        mutableListOf<LenientServicePlanDigestRule>(
            LenientServicePlanDigestVehicleIdLengthRule(64),
            LenientServicePlanDigestAttemptsRangeRule(16, 1000),
            LenientServicePlanDigestRequiredCodeRule("lenient service plan digest needs a value"),
            LenientServicePlanDigestApprovedGuardRule(64),
            LenientServicePlanDigestCodeLengthRule(86400),
        ),
    )
}
