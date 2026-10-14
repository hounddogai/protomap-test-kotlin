package com.acme.transport.lastmile.parcel.parcelvariance

/** A rule a parcel preference must pass. Check returns its problems, or none. */
interface ParcelPreferenceRule {
    fun check(item: ParcelPreference): MutableList<String>
}

data class ParcelPreferenceLabelLengthRule(var maxLength: Int) : ParcelPreferenceRule {
    override fun check(item: ParcelPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelPreferenceCodeLengthRule(var maxLength: Int) : ParcelPreferenceRule {
    override fun check(item: ParcelPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelPreferenceRequiredSummaryRule(var message: String) : ParcelPreferenceRule {
    override fun check(item: ParcelPreference): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelPreferenceRequiredCodeRule(var message: String) : ParcelPreferenceRule {
    override fun check(item: ParcelPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel preference. */
data class ParcelPreferenceValidator(var rules: MutableList<ParcelPreferenceRule>) {
    fun validate(item: ParcelPreference): MutableList<String> {
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

    fun requireValid(item: ParcelPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelPreferenceValidator(): ParcelPreferenceValidator {
    return ParcelPreferenceValidator(
        mutableListOf<ParcelPreferenceRule>(
            ParcelPreferenceLabelLengthRule(64),
            ParcelPreferenceCodeLengthRule(3600),
            ParcelPreferenceRequiredSummaryRule("parcel preference needs an entry"),
            ParcelPreferenceRequiredCodeRule("parcel preference needs an entry"),
        ),
    )
}
