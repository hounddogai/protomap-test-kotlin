package com.acme.transport.customs.tariff.slot

/** A rule a tariff slot must pass. Check returns its problems, or none. */
interface TariffSlotRule {
    fun check(item: TariffSlot): MutableList<String>
}

data class TariffSlotThresholdRangeRule(var min: Double, var max: Double) : TariffSlotRule {
    override fun check(item: TariffSlot): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class TariffSlotRequiredLabelRule(var message: String) : TariffSlotRule {
    override fun check(item: TariffSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffSlotRequiredTimezoneRule(var message: String) : TariffSlotRule {
    override fun check(item: TariffSlot): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffSlotRequiredContactEmailRule(var message: String) : TariffSlotRule {
    override fun check(item: TariffSlot): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffSlotWindowDaysRangeRule(var min: Int, var max: Int) : TariffSlotRule {
    override fun check(item: TariffSlot): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class TariffSlotPinnedGuardRule(var limit: Int) : TariffSlotRule {
    override fun check(item: TariffSlot): MutableList<String> {
        if (item.pinned && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff slot. */
data class TariffSlotValidator(var rules: MutableList<TariffSlotRule>) {
    fun validate(item: TariffSlot): MutableList<String> {
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

    fun requireValid(item: TariffSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffSlotValidator(): TariffSlotValidator {
    return TariffSlotValidator(
        mutableListOf<TariffSlotRule>(
            TariffSlotThresholdRangeRule(4.1, 4862.5),
            TariffSlotRequiredLabelRule("tariff slot needs this field"),
            TariffSlotRequiredTimezoneRule("tariff slot needs this field"),
            TariffSlotRequiredContactEmailRule("tariff slot needs this field"),
            TariffSlotWindowDaysRangeRule(32, 255),
            TariffSlotPinnedGuardRule(86400),
        ),
    )
}
