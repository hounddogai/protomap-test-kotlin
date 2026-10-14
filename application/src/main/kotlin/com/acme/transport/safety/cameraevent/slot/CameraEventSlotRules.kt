package com.acme.transport.safety.cameraevent.slot

/** A rule a camera event slot must pass. Check returns its problems, or none. */
interface CameraEventSlotRule {
    fun check(item: CameraEventSlot): MutableList<String>
}

data class CameraEventSlotShiftCodeLengthRule(var maxLength: Int) : CameraEventSlotRule {
    override fun check(item: CameraEventSlot): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSlotTitleLengthRule(var maxLength: Int) : CameraEventSlotRule {
    override fun check(item: CameraEventSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSlotCodePrefixRule(var prefix: String) : CameraEventSlotRule {
    override fun check(item: CameraEventSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSlotRequiredShiftCodeRule(var message: String) : CameraEventSlotRule {
    override fun check(item: CameraEventSlot): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventSlotMaximumRangeRule(var min: Double, var max: Double) : CameraEventSlotRule {
    override fun check(item: CameraEventSlot): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a camera event slot. */
data class CameraEventSlotValidator(var rules: MutableList<CameraEventSlotRule>) {
    fun validate(item: CameraEventSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventSlotValidator(): CameraEventSlotValidator {
    return CameraEventSlotValidator(
        mutableListOf<CameraEventSlotRule>(
            CameraEventSlotShiftCodeLengthRule(3600),
            CameraEventSlotTitleLengthRule(64),
            CameraEventSlotCodePrefixRule("REF"),
            CameraEventSlotRequiredShiftCodeRule("camera event slot needs a value"),
            CameraEventSlotMaximumRangeRule(2.5, 841.1),
        ),
    )
}
