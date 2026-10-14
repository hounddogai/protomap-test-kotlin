package com.acme.transport.lastmile.parcel.baseline

/** A rule a lenient parcel slot must pass. Check returns its problems, or none. */
interface LenientParcelSlotRule {
    fun check(item: LenientParcelSlot): MutableList<String>
}

data class LenientParcelSlotNameLengthRule(var maxLength: Int) : LenientParcelSlotRule {
    override fun check(item: LenientParcelSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelSlotUpdatedAtRangeRule(var min: Int, var max: Int) : LenientParcelSlotRule {
    override fun check(item: LenientParcelSlot): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelSlotCodeLengthRule(var maxLength: Int) : LenientParcelSlotRule {
    override fun check(item: LenientParcelSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelSlotRequiredNotesRule(var message: String) : LenientParcelSlotRule {
    override fun check(item: LenientParcelSlot): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelSlotCodePrefixRule(var prefix: String) : LenientParcelSlotRule {
    override fun check(item: LenientParcelSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelSlotWeightRangeRule(var min: Double, var max: Double) : LenientParcelSlotRule {
    override fun check(item: LenientParcelSlot): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient parcel slot. */
data class LenientParcelSlotValidator(var rules: MutableList<LenientParcelSlotRule>) {
    fun validate(item: LenientParcelSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientParcelSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientParcelSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientParcelSlotValidator(): LenientParcelSlotValidator {
    return LenientParcelSlotValidator(
        mutableListOf<LenientParcelSlotRule>(
            LenientParcelSlotNameLengthRule(1000),
            LenientParcelSlotUpdatedAtRangeRule(1000, 3600),
            LenientParcelSlotCodeLengthRule(86400),
            LenientParcelSlotRequiredNotesRule("lenient parcel slot needs a value"),
            LenientParcelSlotCodePrefixRule("ACM"),
            LenientParcelSlotWeightRangeRule(6.8, 2754.0),
        ),
    )
}
