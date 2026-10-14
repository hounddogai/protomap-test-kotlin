package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.normalize

/** A rule a adaptive tariff slot must pass. Check returns its problems, or none. */
interface AdaptiveTariffSlotRule {
    fun check(item: AdaptiveTariffSlot): MutableList<String>
}

data class AdaptiveTariffSlotExternalRefLengthRule(var maxLength: Int) : AdaptiveTariffSlotRule {
    override fun check(item: AdaptiveTariffSlot): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffSlotRequiredNotesRule(var message: String) : AdaptiveTariffSlotRule {
    override fun check(item: AdaptiveTariffSlot): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffSlotCapacityRangeRule(var min: Int, var max: Int) : AdaptiveTariffSlotRule {
    override fun check(item: AdaptiveTariffSlot): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffSlotTitleLengthRule(var maxLength: Int) : AdaptiveTariffSlotRule {
    override fun check(item: AdaptiveTariffSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffSlotAllowedExternalRefRule(var allowed: MutableList<String>) : AdaptiveTariffSlotRule {
    override fun check(item: AdaptiveTariffSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tariff slot. */
data class AdaptiveTariffSlotValidator(var rules: MutableList<AdaptiveTariffSlotRule>) {
    fun validate(item: AdaptiveTariffSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTariffSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTariffSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTariffSlotValidator(): AdaptiveTariffSlotValidator {
    return AdaptiveTariffSlotValidator(
        mutableListOf<AdaptiveTariffSlotRule>(
            AdaptiveTariffSlotExternalRefLengthRule(120),
            AdaptiveTariffSlotRequiredNotesRule("adaptive tariff slot needs an entry"),
            AdaptiveTariffSlotCapacityRangeRule(32, 255),
            AdaptiveTariffSlotTitleLengthRule(64),
            AdaptiveTariffSlotAllowedExternalRefRule(mutableListOf<String>()),
        ),
    )
}
