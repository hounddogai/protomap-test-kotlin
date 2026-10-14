package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.text.normalize

/** A rule a manual camera event policy must pass. Check returns its problems, or none. */
interface ManualCameraEventPolicyRule {
    fun check(item: ManualCameraEventPolicy): MutableList<String>
}

data class ManualCameraEventPolicyRequiredNameRule(var message: String) : ManualCameraEventPolicyRule {
    override fun check(item: ManualCameraEventPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventPolicyCeilingRangeRule(
    var min: Double,
    var max: Double,
) : ManualCameraEventPolicyRule {
    override fun check(item: ManualCameraEventPolicy): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class ManualCameraEventPolicyRequiredVehicleIdRule(var message: String) : ManualCameraEventPolicyRule {
    override fun check(item: ManualCameraEventPolicy): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventPolicyAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualCameraEventPolicyRule {
    override fun check(item: ManualCameraEventPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventPolicyRequiredCodeRule(var message: String) : ManualCameraEventPolicyRule {
    override fun check(item: ManualCameraEventPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventPolicyVehicleIdLengthRule(var maxLength: Int) : ManualCameraEventPolicyRule {
    override fun check(item: ManualCameraEventPolicy): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual camera event policy. */
data class ManualCameraEventPolicyValidator(var rules: MutableList<ManualCameraEventPolicyRule>) {
    fun validate(item: ManualCameraEventPolicy): MutableList<String> {
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

    fun requireValid(item: ManualCameraEventPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCameraEventPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCameraEventPolicyValidator(): ManualCameraEventPolicyValidator {
    return ManualCameraEventPolicyValidator(
        mutableListOf<ManualCameraEventPolicyRule>(
            ManualCameraEventPolicyRequiredNameRule("manual camera event policy needs this field"),
            ManualCameraEventPolicyCeilingRangeRule(5.0, 1339.1),
            ManualCameraEventPolicyRequiredVehicleIdRule("manual camera event policy needs this field"),
            ManualCameraEventPolicyAllowedCodeRule(mutableListOf<String>("global")),
            ManualCameraEventPolicyRequiredCodeRule("manual camera event policy needs a value"),
            ManualCameraEventPolicyVehicleIdLengthRule(32),
        ),
    )
}
