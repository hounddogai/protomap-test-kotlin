package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.text.normalize

/** A rule a regional camera event adjustment must pass. Check returns its problems, or none. */
interface RegionalCameraEventAdjustmentRule {
    fun check(item: RegionalCameraEventAdjustment): MutableList<String>
}

data class RegionalCameraEventAdjustmentRequiredNameRule(
    var message: String,
) : RegionalCameraEventAdjustmentRule {
    override fun check(item: RegionalCameraEventAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventAdjustmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : RegionalCameraEventAdjustmentRule {
    override fun check(item: RegionalCameraEventAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventAdjustmentNameLengthRule(var maxLength: Int) : RegionalCameraEventAdjustmentRule {
    override fun check(item: RegionalCameraEventAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCameraEventAdjustmentCodeLengthRule(var maxLength: Int) : RegionalCameraEventAdjustmentRule {
    override fun check(item: RegionalCameraEventAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional camera event adjustment. */
data class RegionalCameraEventAdjustmentValidator(var rules: MutableList<RegionalCameraEventAdjustmentRule>) {
    fun validate(item: RegionalCameraEventAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCameraEventAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCameraEventAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCameraEventAdjustmentValidator(): RegionalCameraEventAdjustmentValidator {
    return RegionalCameraEventAdjustmentValidator(
        mutableListOf<RegionalCameraEventAdjustmentRule>(
            RegionalCameraEventAdjustmentRequiredNameRule("regional camera event adjustment needs this field"),
            RegionalCameraEventAdjustmentAllowedCodeRule(mutableListOf<String>()),
            RegionalCameraEventAdjustmentNameLengthRule(16),
            RegionalCameraEventAdjustmentCodeLengthRule(1000),
        ),
    )
}
