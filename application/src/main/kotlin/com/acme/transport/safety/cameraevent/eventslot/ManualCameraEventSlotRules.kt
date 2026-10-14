package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.text.normalize

/** A rule a manual camera event slot must pass. Check returns its problems, or none. */
interface ManualCameraEventSlotRule {
    fun check(item: ManualCameraEventSlot): MutableList<String>
}

data class ManualCameraEventSlotAllowedLabelRule(var allowed: MutableList<String>) : ManualCameraEventSlotRule {
    override fun check(item: ManualCameraEventSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventSlotRequiredLabelRule(var message: String) : ManualCameraEventSlotRule {
    override fun check(item: ManualCameraEventSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventSlotRequiredCodeRule(var message: String) : ManualCameraEventSlotRule {
    override fun check(item: ManualCameraEventSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCameraEventSlotPriorityRangeRule(var min: Int, var max: Int) : ManualCameraEventSlotRule {
    override fun check(item: ManualCameraEventSlot): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual camera event slot. */
data class ManualCameraEventSlotValidator(var rules: MutableList<ManualCameraEventSlotRule>) {
    fun validate(item: ManualCameraEventSlot): MutableList<String> {
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

    fun requireValid(item: ManualCameraEventSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCameraEventSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCameraEventSlotValidator(): ManualCameraEventSlotValidator {
    return ManualCameraEventSlotValidator(
        mutableListOf<ManualCameraEventSlotRule>(
            ManualCameraEventSlotAllowedLabelRule(mutableListOf<String>("lenient", "pending")),
            ManualCameraEventSlotRequiredLabelRule("manual camera event slot needs a value"),
            ManualCameraEventSlotRequiredCodeRule("manual camera event slot needs a value"),
            ManualCameraEventSlotPriorityRangeRule(80, 120),
        ),
    )
}
