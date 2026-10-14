package com.acme.transport.freightpay.accessorial.accessorialadjustment

/** A rule a regional accessorial slot must pass. Check returns its problems, or none. */
interface RegionalAccessorialSlotRule {
    fun check(item: RegionalAccessorialSlot): MutableList<String>
}

data class RegionalAccessorialSlotLaneLengthRule(var maxLength: Int) : RegionalAccessorialSlotRule {
    override fun check(item: RegionalAccessorialSlot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAccessorialSlotRequiredLabelRule(var message: String) : RegionalAccessorialSlotRule {
    override fun check(item: RegionalAccessorialSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAccessorialSlotMaximumRangeRule(
    var min: Double,
    var max: Double,
) : RegionalAccessorialSlotRule {
    override fun check(item: RegionalAccessorialSlot): MutableList<String> {
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

data class RegionalAccessorialSlotRequiredNotesRule(var message: String) : RegionalAccessorialSlotRule {
    override fun check(item: RegionalAccessorialSlot): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAccessorialSlotLabelLengthRule(var maxLength: Int) : RegionalAccessorialSlotRule {
    override fun check(item: RegionalAccessorialSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional accessorial slot. */
data class RegionalAccessorialSlotValidator(var rules: MutableList<RegionalAccessorialSlotRule>) {
    fun validate(item: RegionalAccessorialSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAccessorialSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAccessorialSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAccessorialSlotValidator(): RegionalAccessorialSlotValidator {
    return RegionalAccessorialSlotValidator(
        mutableListOf<RegionalAccessorialSlotRule>(
            RegionalAccessorialSlotLaneLengthRule(80),
            RegionalAccessorialSlotRequiredLabelRule("regional accessorial slot needs a value"),
            RegionalAccessorialSlotMaximumRangeRule(4.2, 116.4),
            RegionalAccessorialSlotRequiredNotesRule("regional accessorial slot needs a value"),
            RegionalAccessorialSlotLabelLengthRule(64),
        ),
    )
}
