package com.acme.transport.fleetops.defect.defectmapping

/** A rule a pending defect slot must pass. Check returns its problems, or none. */
interface PendingDefectSlotRule {
    fun check(item: PendingDefectSlot): MutableList<String>
}

data class PendingDefectSlotChecksumLengthRule(var maxLength: Int) : PendingDefectSlotRule {
    override fun check(item: PendingDefectSlot): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectSlotFloorValueRangeRule(var min: Double, var max: Double) : PendingDefectSlotRule {
    override fun check(item: PendingDefectSlot): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectSlotRequiredChannelRule(var message: String) : PendingDefectSlotRule {
    override fun check(item: PendingDefectSlot): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectSlotArchivedGuardRule(var limit: Int) : PendingDefectSlotRule {
    override fun check(item: PendingDefectSlot): MutableList<String> {
        if (item.archived && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectSlotRequiredCodeRule(var message: String) : PendingDefectSlotRule {
    override fun check(item: PendingDefectSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectSlotLabelLengthRule(var maxLength: Int) : PendingDefectSlotRule {
    override fun check(item: PendingDefectSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending defect slot. */
data class PendingDefectSlotValidator(var rules: MutableList<PendingDefectSlotRule>) {
    fun validate(item: PendingDefectSlot): MutableList<String> {
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

    fun requireValid(item: PendingDefectSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDefectSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDefectSlotValidator(): PendingDefectSlotValidator {
    return PendingDefectSlotValidator(
        mutableListOf<PendingDefectSlotRule>(
            PendingDefectSlotChecksumLengthRule(32),
            PendingDefectSlotFloorValueRangeRule(2.1, 1049.1),
            PendingDefectSlotRequiredChannelRule("pending defect slot needs a value"),
            PendingDefectSlotArchivedGuardRule(255),
            PendingDefectSlotRequiredCodeRule("pending defect slot needs an entry"),
            PendingDefectSlotLabelLengthRule(16),
        ),
    )
}
