package com.acme.transport.lastmile.parcel.segment

/** A rule a parcel entry must pass. Check returns its problems, or none. */
interface ParcelEntryRule {
    fun check(item: ParcelEntry): MutableList<String>
}

data class ParcelEntryGraceDaysRangeRule(var min: Int, var max: Int) : ParcelEntryRule {
    override fun check(item: ParcelEntry): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class ParcelEntryCodePrefixRule(var prefix: String) : ParcelEntryRule {
    override fun check(item: ParcelEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ParcelEntryRequiredCodeRule(var message: String) : ParcelEntryRule {
    override fun check(item: ParcelEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelEntryDescriptionLengthRule(var maxLength: Int) : ParcelEntryRule {
    override fun check(item: ParcelEntry): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel entry. */
data class ParcelEntryValidator(var rules: MutableList<ParcelEntryRule>) {
    fun validate(item: ParcelEntry): MutableList<String> {
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

    fun requireValid(item: ParcelEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelEntryValidator(): ParcelEntryValidator {
    return ParcelEntryValidator(
        mutableListOf<ParcelEntryRule>(
            ParcelEntryGraceDaysRangeRule(86400, 86400),
            ParcelEntryCodePrefixRule("X"),
            ParcelEntryRequiredCodeRule("parcel entry needs a value"),
            ParcelEntryDescriptionLengthRule(120),
        ),
    )
}
