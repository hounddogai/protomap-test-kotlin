package com.acme.transport.lastmile.locker.mapping

/** A rule a global locker slot must pass. Check returns its problems, or none. */
interface GlobalLockerSlotRule {
    fun check(item: GlobalLockerSlot): MutableList<String>
}

data class GlobalLockerSlotRateRangeRule(var min: Double, var max: Double) : GlobalLockerSlotRule {
    override fun check(item: GlobalLockerSlot): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerSlotDescriptionLengthRule(var maxLength: Int) : GlobalLockerSlotRule {
    override fun check(item: GlobalLockerSlot): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerSlotCodeLengthRule(var maxLength: Int) : GlobalLockerSlotRule {
    override fun check(item: GlobalLockerSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerSlotAttemptsRangeRule(var min: Int, var max: Int) : GlobalLockerSlotRule {
    override fun check(item: GlobalLockerSlot): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerSlotTitleLengthRule(var maxLength: Int) : GlobalLockerSlotRule {
    override fun check(item: GlobalLockerSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLockerSlotRequiredTitleRule(var message: String) : GlobalLockerSlotRule {
    override fun check(item: GlobalLockerSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global locker slot. */
data class GlobalLockerSlotValidator(var rules: MutableList<GlobalLockerSlotRule>) {
    fun validate(item: GlobalLockerSlot): MutableList<String> {
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

    fun requireValid(item: GlobalLockerSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLockerSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLockerSlotValidator(): GlobalLockerSlotValidator {
    return GlobalLockerSlotValidator(
        mutableListOf<GlobalLockerSlotRule>(
            GlobalLockerSlotRateRangeRule(7.6, 4745.6),
            GlobalLockerSlotDescriptionLengthRule(255),
            GlobalLockerSlotCodeLengthRule(1000),
            GlobalLockerSlotAttemptsRangeRule(120, 120),
            GlobalLockerSlotTitleLengthRule(32),
            GlobalLockerSlotRequiredTitleRule("global locker slot needs an entry"),
        ),
    )
}
