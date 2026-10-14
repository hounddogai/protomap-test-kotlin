package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.text.normalize

/** A rule a manual clearance notice must pass. Check returns its problems, or none. */
interface ManualClearanceNoticeRule {
    fun check(item: ManualClearanceNotice): MutableList<String>
}

data class ManualClearanceNoticeRequiredVehicleIdRule(var message: String) : ManualClearanceNoticeRule {
    override fun check(item: ManualClearanceNotice): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceNoticeVersionRangeRule(var min: Int, var max: Int) : ManualClearanceNoticeRule {
    override fun check(item: ManualClearanceNotice): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceNoticeAllowedSourceRule(
    var allowed: MutableList<String>,
) : ManualClearanceNoticeRule {
    override fun check(item: ManualClearanceNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceNoticeCodePrefixRule(var prefix: String) : ManualClearanceNoticeRule {
    override fun check(item: ManualClearanceNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceNoticeRequiredLabelRule(var message: String) : ManualClearanceNoticeRule {
    override fun check(item: ManualClearanceNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualClearanceNoticeSourceLengthRule(var maxLength: Int) : ManualClearanceNoticeRule {
    override fun check(item: ManualClearanceNotice): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual clearance notice. */
data class ManualClearanceNoticeValidator(var rules: MutableList<ManualClearanceNoticeRule>) {
    fun validate(item: ManualClearanceNotice): MutableList<String> {
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

    fun requireValid(item: ManualClearanceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualClearanceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultManualClearanceNoticeValidator(): ManualClearanceNoticeValidator {
    return ManualClearanceNoticeValidator(
        mutableListOf<ManualClearanceNoticeRule>(
            ManualClearanceNoticeRequiredVehicleIdRule("manual clearance notice needs a value"),
            ManualClearanceNoticeVersionRangeRule(16, 3600),
            ManualClearanceNoticeAllowedSourceRule(mutableListOf<String>("weighted", "manual", "strict")),
            ManualClearanceNoticeCodePrefixRule("ACM"),
            ManualClearanceNoticeRequiredLabelRule("manual clearance notice needs a value"),
            ManualClearanceNoticeSourceLengthRule(64),
        ),
    )
}
