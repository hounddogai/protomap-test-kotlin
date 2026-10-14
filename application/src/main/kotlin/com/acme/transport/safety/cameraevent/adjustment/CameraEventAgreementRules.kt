package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a camera event agreement must pass. Check returns its problems, or none. */
interface CameraEventAgreementRule {
    fun check(item: CameraEventAgreement): MutableList<String>
}

data class CameraEventAgreementRequiredCodeRule(var message: String) : CameraEventAgreementRule {
    override fun check(item: CameraEventAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAgreementAmountRangeRule(var min: Double, var max: Double) : CameraEventAgreementRule {
    override fun check(item: CameraEventAgreement): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class CameraEventAgreementCeilingRangeRule(var min: Double, var max: Double) : CameraEventAgreementRule {
    override fun check(item: CameraEventAgreement): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAgreementCodePrefixRule(var prefix: String) : CameraEventAgreementRule {
    override fun check(item: CameraEventAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAgreementAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : CameraEventAgreementRule {
    override fun check(item: CameraEventAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAgreementEndsAtRangeRule(var min: Int, var max: Int) : CameraEventAgreementRule {
    override fun check(item: CameraEventAgreement): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a camera event agreement. */
data class CameraEventAgreementValidator(var rules: MutableList<CameraEventAgreementRule>) {
    fun validate(item: CameraEventAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventAgreementValidator(): CameraEventAgreementValidator {
    return CameraEventAgreementValidator(
        mutableListOf<CameraEventAgreementRule>(
            CameraEventAgreementRequiredCodeRule("camera event agreement needs this field"),
            CameraEventAgreementAmountRangeRule(9.7, 2379.9),
            CameraEventAgreementCeilingRangeRule(0.2, 1532.7),
            CameraEventAgreementCodePrefixRule("X"),
            CameraEventAgreementAllowedRouteCodeRule(mutableListOf<String>()),
            CameraEventAgreementEndsAtRangeRule(32, 120),
        ),
    )
}
