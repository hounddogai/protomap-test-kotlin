package com.acme.transport.lastmile.parcel.parcelmapping

/** A rule a regional parcel mapping must pass. Check returns its problems, or none. */
interface RegionalParcelMappingRule {
    fun check(item: RegionalParcelMapping): MutableList<String>
}

data class RegionalParcelMappingRequiredNotesRule(var message: String) : RegionalParcelMappingRule {
    override fun check(item: RegionalParcelMapping): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelMappingRegionLengthRule(var maxLength: Int) : RegionalParcelMappingRule {
    override fun check(item: RegionalParcelMapping): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelMappingRequiredRegionRule(var message: String) : RegionalParcelMappingRule {
    override fun check(item: RegionalParcelMapping): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional parcel mapping. */
data class RegionalParcelMappingValidator(var rules: MutableList<RegionalParcelMappingRule>) {
    fun validate(item: RegionalParcelMapping): MutableList<String> {
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

    fun requireValid(item: RegionalParcelMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalParcelMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalParcelMappingValidator(): RegionalParcelMappingValidator {
    return RegionalParcelMappingValidator(
        mutableListOf<RegionalParcelMappingRule>(
            RegionalParcelMappingRequiredNotesRule("regional parcel mapping needs this field"),
            RegionalParcelMappingRegionLengthRule(1000),
            RegionalParcelMappingRequiredRegionRule("regional parcel mapping needs a value"),
        ),
    )
}
