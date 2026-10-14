package com.acme.transport.emissions.evcharge.slot

/** A rule a ev charge slot must pass. Check returns its problems, or none. */
interface EvChargeSlotRule {
    fun check(item: EvChargeSlot): MutableList<String>
}

data class EvChargeSlotChecksumLengthRule(var maxLength: Int) : EvChargeSlotRule {
    override fun check(item: EvChargeSlot): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSlotRequiredSourceRule(var message: String) : EvChargeSlotRule {
    override fun check(item: EvChargeSlot): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSlotNameLengthRule(var maxLength: Int) : EvChargeSlotRule {
    override fun check(item: EvChargeSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSlotEtaMinutesRangeRule(var min: Int, var max: Int) : EvChargeSlotRule {
    override fun check(item: EvChargeSlot): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSlotRequiredCodeRule(var message: String) : EvChargeSlotRule {
    override fun check(item: EvChargeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeSlotRequiredNameRule(var message: String) : EvChargeSlotRule {
    override fun check(item: EvChargeSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge slot. */
data class EvChargeSlotValidator(var rules: MutableList<EvChargeSlotRule>) {
    fun validate(item: EvChargeSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeSlotValidator(): EvChargeSlotValidator {
    return EvChargeSlotValidator(
        mutableListOf<EvChargeSlotRule>(
            EvChargeSlotChecksumLengthRule(3600),
            EvChargeSlotRequiredSourceRule("ev charge slot needs a value"),
            EvChargeSlotNameLengthRule(86400),
            EvChargeSlotEtaMinutesRangeRule(80, 1000),
            EvChargeSlotRequiredCodeRule("ev charge slot needs this field"),
            EvChargeSlotRequiredNameRule("ev charge slot needs a value"),
        ),
    )
}
