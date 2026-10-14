package com.acme.transport.emissions.idling.window

/** A rule a lenient idling slot must pass. Check returns its problems, or none. */
interface LenientIdlingSlotRule {
    fun check(item: LenientIdlingSlot): MutableList<String>
}

data class LenientIdlingSlotRevisionRangeRule(var min: Int, var max: Int) : LenientIdlingSlotRule {
    override fun check(item: LenientIdlingSlot): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class LenientIdlingSlotRequiredLabelRule(var message: String) : LenientIdlingSlotRule {
    override fun check(item: LenientIdlingSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingSlotMinimumRangeRule(var min: Double, var max: Double) : LenientIdlingSlotRule {
    override fun check(item: LenientIdlingSlot): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientIdlingSlotNotesLengthRule(var maxLength: Int) : LenientIdlingSlotRule {
    override fun check(item: LenientIdlingSlot): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient idling slot. */
data class LenientIdlingSlotValidator(var rules: MutableList<LenientIdlingSlotRule>) {
    fun validate(item: LenientIdlingSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientIdlingSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientIdlingSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientIdlingSlotValidator(): LenientIdlingSlotValidator {
    return LenientIdlingSlotValidator(
        mutableListOf<LenientIdlingSlotRule>(
            LenientIdlingSlotRevisionRangeRule(80, 1000),
            LenientIdlingSlotRequiredLabelRule("lenient idling slot needs an entry"),
            LenientIdlingSlotMinimumRangeRule(4.5, 1388.2),
            LenientIdlingSlotNotesLengthRule(80),
        ),
    )
}
