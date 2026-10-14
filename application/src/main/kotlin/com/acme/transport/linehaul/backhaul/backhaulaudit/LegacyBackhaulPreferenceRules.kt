package com.acme.transport.linehaul.backhaul.backhaulaudit

/** A rule a legacy backhaul preference must pass. Check returns its problems, or none. */
interface LegacyBackhaulPreferenceRule {
    fun check(item: LegacyBackhaulPreference): MutableList<String>
}

data class LegacyBackhaulPreferenceOwnerIdLengthRule(var maxLength: Int) : LegacyBackhaulPreferenceRule {
    override fun check(item: LegacyBackhaulPreference): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulPreferenceRequiredSegmentRule(var message: String) : LegacyBackhaulPreferenceRule {
    override fun check(item: LegacyBackhaulPreference): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulPreferenceRequiredLabelRule(var message: String) : LegacyBackhaulPreferenceRule {
    override fun check(item: LegacyBackhaulPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulPreferenceFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : LegacyBackhaulPreferenceRule {
    override fun check(item: LegacyBackhaulPreference): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy backhaul preference. */
data class LegacyBackhaulPreferenceValidator(var rules: MutableList<LegacyBackhaulPreferenceRule>) {
    fun validate(item: LegacyBackhaulPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyBackhaulPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBackhaulPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBackhaulPreferenceValidator(): LegacyBackhaulPreferenceValidator {
    return LegacyBackhaulPreferenceValidator(
        mutableListOf<LegacyBackhaulPreferenceRule>(
            LegacyBackhaulPreferenceOwnerIdLengthRule(120),
            LegacyBackhaulPreferenceRequiredSegmentRule("legacy backhaul preference needs a value"),
            LegacyBackhaulPreferenceRequiredLabelRule("legacy backhaul preference needs a value"),
            LegacyBackhaulPreferenceFloorValueRangeRule(7.9, 4703.3),
        ),
    )
}
