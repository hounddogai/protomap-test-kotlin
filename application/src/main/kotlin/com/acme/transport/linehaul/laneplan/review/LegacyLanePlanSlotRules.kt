package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.text.normalize

/** A rule a legacy lane plan slot must pass. Check returns its problems, or none. */
interface LegacyLanePlanSlotRule {
    fun check(item: LegacyLanePlanSlot): MutableList<String>
}

data class LegacyLanePlanSlotCodeLengthRule(var maxLength: Int) : LegacyLanePlanSlotRule {
    override fun check(item: LegacyLanePlanSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanSlotRequiredVehicleIdRule(var message: String) : LegacyLanePlanSlotRule {
    override fun check(item: LegacyLanePlanSlot): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanSlotNameLengthRule(var maxLength: Int) : LegacyLanePlanSlotRule {
    override fun check(item: LegacyLanePlanSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanSlotAllowedCodeRule(var allowed: MutableList<String>) : LegacyLanePlanSlotRule {
    override fun check(item: LegacyLanePlanSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanSlotRequiredCodeRule(var message: String) : LegacyLanePlanSlotRule {
    override fun check(item: LegacyLanePlanSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanSlotCodePrefixRule(var prefix: String) : LegacyLanePlanSlotRule {
    override fun check(item: LegacyLanePlanSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy lane plan slot. */
data class LegacyLanePlanSlotValidator(var rules: MutableList<LegacyLanePlanSlotRule>) {
    fun validate(item: LegacyLanePlanSlot): MutableList<String> {
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

    fun requireValid(item: LegacyLanePlanSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLanePlanSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLanePlanSlotValidator(): LegacyLanePlanSlotValidator {
    return LegacyLanePlanSlotValidator(
        mutableListOf<LegacyLanePlanSlotRule>(
            LegacyLanePlanSlotCodeLengthRule(16),
            LegacyLanePlanSlotRequiredVehicleIdRule("legacy lane plan slot needs a value"),
            LegacyLanePlanSlotNameLengthRule(1000),
            LegacyLanePlanSlotAllowedCodeRule(mutableListOf<String>()),
            LegacyLanePlanSlotRequiredCodeRule("legacy lane plan slot needs a value"),
            LegacyLanePlanSlotCodePrefixRule("Q"),
        ),
    )
}
