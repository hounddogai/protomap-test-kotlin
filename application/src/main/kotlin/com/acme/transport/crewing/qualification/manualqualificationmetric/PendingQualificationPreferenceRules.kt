package com.acme.transport.crewing.qualification.manualqualificationmetric

/** A rule a pending qualification preference must pass. Check returns its problems, or none. */
interface PendingQualificationPreferenceRule {
    fun check(item: PendingQualificationPreference): MutableList<String>
}

data class PendingQualificationPreferenceTitleLengthRule(
    var maxLength: Int,
) : PendingQualificationPreferenceRule {
    override fun check(item: PendingQualificationPreference): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationPreferenceRequiredVehicleIdRule(
    var message: String,
) : PendingQualificationPreferenceRule {
    override fun check(item: PendingQualificationPreference): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationPreferenceCodeLengthRule(
    var maxLength: Int,
) : PendingQualificationPreferenceRule {
    override fun check(item: PendingQualificationPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationPreferenceRequiredCodeRule(
    var message: String,
) : PendingQualificationPreferenceRule {
    override fun check(item: PendingQualificationPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationPreferenceRequiredNameRule(
    var message: String,
) : PendingQualificationPreferenceRule {
    override fun check(item: PendingQualificationPreference): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingQualificationPreferenceLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PendingQualificationPreferenceRule {
    override fun check(item: PendingQualificationPreference): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending qualification preference. */
data class PendingQualificationPreferenceValidator(var rules: MutableList<PendingQualificationPreferenceRule>) {
    fun validate(item: PendingQualificationPreference): MutableList<String> {
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

    fun requireValid(item: PendingQualificationPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingQualificationPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingQualificationPreferenceValidator(): PendingQualificationPreferenceValidator {
    return PendingQualificationPreferenceValidator(
        mutableListOf<PendingQualificationPreferenceRule>(
            PendingQualificationPreferenceTitleLengthRule(64),
            PendingQualificationPreferenceRequiredVehicleIdRule(
                "pending qualification preference needs an entry",
            ),
            PendingQualificationPreferenceCodeLengthRule(1000),
            PendingQualificationPreferenceRequiredCodeRule("pending qualification preference needs a value"),
            PendingQualificationPreferenceRequiredNameRule("pending qualification preference needs a value"),
            PendingQualificationPreferenceLoadKgRangeRule(3.3, 2937.9),
        ),
    )
}
