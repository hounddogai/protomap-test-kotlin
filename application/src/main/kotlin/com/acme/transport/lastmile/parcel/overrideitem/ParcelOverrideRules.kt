package com.acme.transport.lastmile.parcel.overrideitem

/** A rule a parcel override must pass. Check returns its problems, or none. */
interface ParcelOverrideRule {
    fun check(item: ParcelOverride): MutableList<String>
}

data class ParcelOverrideTitleLengthRule(var maxLength: Int) : ParcelOverrideRule {
    override fun check(item: ParcelOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelOverrideRequiredCodeRule(var message: String) : ParcelOverrideRule {
    override fun check(item: ParcelOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelOverrideFloorValueRangeRule(var min: Double, var max: Double) : ParcelOverrideRule {
    override fun check(item: ParcelOverride): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel override. */
data class ParcelOverrideValidator(var rules: MutableList<ParcelOverrideRule>) {
    fun validate(item: ParcelOverride): MutableList<String> {
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

    fun requireValid(item: ParcelOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelOverrideValidator(): ParcelOverrideValidator {
    return ParcelOverrideValidator(
        mutableListOf<ParcelOverrideRule>(
            ParcelOverrideTitleLengthRule(16),
            ParcelOverrideRequiredCodeRule("parcel override needs this field"),
            ParcelOverrideFloorValueRangeRule(8.8, 579.9),
        ),
    )
}
