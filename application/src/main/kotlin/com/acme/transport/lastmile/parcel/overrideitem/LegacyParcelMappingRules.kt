package com.acme.transport.lastmile.parcel.overrideitem

/** A rule a legacy parcel mapping must pass. Check returns its problems, or none. */
interface LegacyParcelMappingRule {
    fun check(item: LegacyParcelMapping): MutableList<String>
}

data class LegacyParcelMappingRequiredLabelRule(var message: String) : LegacyParcelMappingRule {
    override fun check(item: LegacyParcelMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelMappingLabelLengthRule(var maxLength: Int) : LegacyParcelMappingRule {
    override fun check(item: LegacyParcelMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelMappingRequiredShiftCodeRule(var message: String) : LegacyParcelMappingRule {
    override fun check(item: LegacyParcelMapping): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy parcel mapping. */
data class LegacyParcelMappingValidator(var rules: MutableList<LegacyParcelMappingRule>) {
    fun validate(item: LegacyParcelMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyParcelMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyParcelMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyParcelMappingValidator(): LegacyParcelMappingValidator {
    return LegacyParcelMappingValidator(
        mutableListOf<LegacyParcelMappingRule>(
            LegacyParcelMappingRequiredLabelRule("legacy parcel mapping needs a value"),
            LegacyParcelMappingLabelLengthRule(64),
            LegacyParcelMappingRequiredShiftCodeRule("legacy parcel mapping needs a value"),
        ),
    )
}
