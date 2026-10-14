package com.acme.transport.fleetops.defect.allocation

/** A rule a defect slot must pass. Check returns its problems, or none. */
interface DefectSlotRule {
    fun check(item: DefectSlot): MutableList<String>
}

data class DefectSlotLabelLengthRule(var maxLength: Int) : DefectSlotRule {
    override fun check(item: DefectSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectSlotDescriptionLengthRule(var maxLength: Int) : DefectSlotRule {
    override fun check(item: DefectSlot): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectSlotCodePrefixRule(var prefix: String) : DefectSlotRule {
    override fun check(item: DefectSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect slot. */
data class DefectSlotValidator(var rules: MutableList<DefectSlotRule>) {
    fun validate(item: DefectSlot): MutableList<String> {
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

    fun requireValid(item: DefectSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectSlotValidator(): DefectSlotValidator {
    return DefectSlotValidator(
        mutableListOf<DefectSlotRule>(
            DefectSlotLabelLengthRule(64),
            DefectSlotDescriptionLengthRule(3600),
            DefectSlotCodePrefixRule("ACM"),
        ),
    )
}
