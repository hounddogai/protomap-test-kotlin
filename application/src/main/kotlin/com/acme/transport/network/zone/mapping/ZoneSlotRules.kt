package com.acme.transport.network.zone.mapping

/** A rule a zone slot must pass. Check returns its problems, or none. */
interface ZoneSlotRule {
    fun check(item: ZoneSlot): MutableList<String>
}

data class ZoneSlotRequiredLocaleRule(var message: String) : ZoneSlotRule {
    override fun check(item: ZoneSlot): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneSlotEndsAtRangeRule(var min: Int, var max: Int) : ZoneSlotRule {
    override fun check(item: ZoneSlot): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneSlotActiveGuardRule(var limit: Double) : ZoneSlotRule {
    override fun check(item: ZoneSlot): MutableList<String> {
        if (item.active && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ZoneSlotLocaleLengthRule(var maxLength: Int) : ZoneSlotRule {
    override fun check(item: ZoneSlot): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone slot. */
data class ZoneSlotValidator(var rules: MutableList<ZoneSlotRule>) {
    fun validate(item: ZoneSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneSlotValidator(): ZoneSlotValidator {
    return ZoneSlotValidator(
        mutableListOf<ZoneSlotRule>(
            ZoneSlotRequiredLocaleRule("zone slot needs an entry"),
            ZoneSlotEndsAtRangeRule(255, 255),
            ZoneSlotActiveGuardRule(0.2),
            ZoneSlotLocaleLengthRule(64),
        ),
    )
}
