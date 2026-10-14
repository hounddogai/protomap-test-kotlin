package com.acme.transport.safety.cameraevent.cycle

/** A rule a camera event cycle must pass. Check returns its problems, or none. */
interface CameraEventCycleRule {
    fun check(item: CameraEventCycle): MutableList<String>
}

data class CameraEventCycleLabelLengthRule(var maxLength: Int) : CameraEventCycleRule {
    override fun check(item: CameraEventCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventCycleSequenceRangeRule(var min: Int, var max: Int) : CameraEventCycleRule {
    override fun check(item: CameraEventCycle): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventCycleCodeLengthRule(var maxLength: Int) : CameraEventCycleRule {
    override fun check(item: CameraEventCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventCycleStopCountRangeRule(var min: Int, var max: Int) : CameraEventCycleRule {
    override fun check(item: CameraEventCycle): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventCycleCodePrefixRule(var prefix: String) : CameraEventCycleRule {
    override fun check(item: CameraEventCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event cycle. */
data class CameraEventCycleValidator(var rules: MutableList<CameraEventCycleRule>) {
    fun validate(item: CameraEventCycle): MutableList<String> {
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

    fun requireValid(item: CameraEventCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventCycleValidator(): CameraEventCycleValidator {
    return CameraEventCycleValidator(
        mutableListOf<CameraEventCycleRule>(
            CameraEventCycleLabelLengthRule(3600),
            CameraEventCycleSequenceRangeRule(120, 1000),
            CameraEventCycleCodeLengthRule(255),
            CameraEventCycleStopCountRangeRule(80, 255),
            CameraEventCycleCodePrefixRule("REF"),
        ),
    )
}
