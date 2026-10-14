package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.kit.text.normalize

/** A rule a regional parcel slot must pass. Check returns its problems, or none. */
interface RegionalParcelSlotRule {
    fun check(item: RegionalParcelSlot): MutableList<String>
}

data class RegionalParcelSlotRequiredTitleRule(var message: String) : RegionalParcelSlotRule {
    override fun check(item: RegionalParcelSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelSlotTitleLengthRule(var maxLength: Int) : RegionalParcelSlotRule {
    override fun check(item: RegionalParcelSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelSlotRequiredExternalRefRule(var message: String) : RegionalParcelSlotRule {
    override fun check(item: RegionalParcelSlot): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelSlotAllowedCodeRule(var allowed: MutableList<String>) : RegionalParcelSlotRule {
    override fun check(item: RegionalParcelSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional parcel slot. */
data class RegionalParcelSlotValidator(var rules: MutableList<RegionalParcelSlotRule>) {
    fun validate(item: RegionalParcelSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalParcelSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalParcelSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalParcelSlotValidator(): RegionalParcelSlotValidator {
    return RegionalParcelSlotValidator(
        mutableListOf<RegionalParcelSlotRule>(
            RegionalParcelSlotRequiredTitleRule("regional parcel slot needs this field"),
            RegionalParcelSlotTitleLengthRule(86400),
            RegionalParcelSlotRequiredExternalRefRule("regional parcel slot needs an entry"),
            RegionalParcelSlotAllowedCodeRule(mutableListOf<String>("primary")),
        ),
    )
}
