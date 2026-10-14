package com.acme.transport.safety.cameraevent.batch

/** A rule a camera event plan must pass. Check returns its problems, or none. */
interface CameraEventPlanRule {
    fun check(item: CameraEventPlan): MutableList<String>
}

data class CameraEventPlanCodePrefixRule(var prefix: String) : CameraEventPlanRule {
    override fun check(item: CameraEventPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventPlanRequiredChecksumRule(var message: String) : CameraEventPlanRule {
    override fun check(item: CameraEventPlan): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventPlanNameLengthRule(var maxLength: Int) : CameraEventPlanRule {
    override fun check(item: CameraEventPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventPlanQuantityRangeRule(var min: Int, var max: Int) : CameraEventPlanRule {
    override fun check(item: CameraEventPlan): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventPlanChecksumLengthRule(var maxLength: Int) : CameraEventPlanRule {
    override fun check(item: CameraEventPlan): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event plan. */
data class CameraEventPlanValidator(var rules: MutableList<CameraEventPlanRule>) {
    fun validate(item: CameraEventPlan): MutableList<String> {
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

    fun requireValid(item: CameraEventPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventPlanValidator(): CameraEventPlanValidator {
    return CameraEventPlanValidator(
        mutableListOf<CameraEventPlanRule>(
            CameraEventPlanCodePrefixRule("REF"),
            CameraEventPlanRequiredChecksumRule("camera event plan needs an entry"),
            CameraEventPlanNameLengthRule(1000),
            CameraEventPlanQuantityRangeRule(120, 1000),
            CameraEventPlanChecksumLengthRule(3600),
        ),
    )
}
