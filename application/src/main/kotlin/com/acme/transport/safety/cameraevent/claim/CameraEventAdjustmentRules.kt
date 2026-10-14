package com.acme.transport.safety.cameraevent.claim

/** A rule a camera event adjustment must pass. Check returns its problems, or none. */
interface CameraEventAdjustmentRule {
    fun check(item: CameraEventAdjustment): MutableList<String>
}

data class CameraEventAdjustmentCodePrefixRule(var prefix: String) : CameraEventAdjustmentRule {
    override fun check(item: CameraEventAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAdjustmentRequiredUnitRule(var message: String) : CameraEventAdjustmentRule {
    override fun check(item: CameraEventAdjustment): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventAdjustmentTitleLengthRule(var maxLength: Int) : CameraEventAdjustmentRule {
    override fun check(item: CameraEventAdjustment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event adjustment. */
data class CameraEventAdjustmentValidator(var rules: MutableList<CameraEventAdjustmentRule>) {
    fun validate(item: CameraEventAdjustment): MutableList<String> {
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

    fun requireValid(item: CameraEventAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventAdjustmentValidator(): CameraEventAdjustmentValidator {
    return CameraEventAdjustmentValidator(
        mutableListOf<CameraEventAdjustmentRule>(
            CameraEventAdjustmentCodePrefixRule("REF"),
            CameraEventAdjustmentRequiredUnitRule("camera event adjustment needs this field"),
            CameraEventAdjustmentTitleLengthRule(120),
        ),
    )
}
