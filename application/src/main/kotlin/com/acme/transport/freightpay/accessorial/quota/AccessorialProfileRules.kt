package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.kit.text.normalize

/** A rule a accessorial profile must pass. Check returns its problems, or none. */
interface AccessorialProfileRule {
    fun check(item: AccessorialProfile): MutableList<String>
}

data class AccessorialProfileCodeLengthRule(var maxLength: Int) : AccessorialProfileRule {
    override fun check(item: AccessorialProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialProfileAllowedCodeRule(var allowed: MutableList<String>) : AccessorialProfileRule {
    override fun check(item: AccessorialProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialProfileChecksumLengthRule(var maxLength: Int) : AccessorialProfileRule {
    override fun check(item: AccessorialProfile): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialProfileLimitRangeRule(var min: Int, var max: Int) : AccessorialProfileRule {
    override fun check(item: AccessorialProfile): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class AccessorialProfileVehicleIdLengthRule(var maxLength: Int) : AccessorialProfileRule {
    override fun check(item: AccessorialProfile): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialProfileRequiredCodeRule(var message: String) : AccessorialProfileRule {
    override fun check(item: AccessorialProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial profile. */
data class AccessorialProfileValidator(var rules: MutableList<AccessorialProfileRule>) {
    fun validate(item: AccessorialProfile): MutableList<String> {
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

    fun requireValid(item: AccessorialProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialProfileValidator(): AccessorialProfileValidator {
    return AccessorialProfileValidator(
        mutableListOf<AccessorialProfileRule>(
            AccessorialProfileCodeLengthRule(120),
            AccessorialProfileAllowedCodeRule(mutableListOf<String>("strict")),
            AccessorialProfileChecksumLengthRule(16),
            AccessorialProfileLimitRangeRule(16, 32),
            AccessorialProfileVehicleIdLengthRule(86400),
            AccessorialProfileRequiredCodeRule("accessorial profile needs a value"),
        ),
    )
}
