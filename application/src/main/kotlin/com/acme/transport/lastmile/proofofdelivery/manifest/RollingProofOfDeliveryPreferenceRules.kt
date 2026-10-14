package com.acme.transport.lastmile.proofofdelivery.manifest

/** A rule a rolling proof of delivery preference must pass. Check returns its problems, or none. */
interface RollingProofOfDeliveryPreferenceRule {
    fun check(item: RollingProofOfDeliveryPreference): MutableList<String>
}

data class RollingProofOfDeliveryPreferenceFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingProofOfDeliveryPreferenceRule {
    override fun check(item: RollingProofOfDeliveryPreference): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class RollingProofOfDeliveryPreferenceRequiredCarrierRule(
    var message: String,
) : RollingProofOfDeliveryPreferenceRule {
    override fun check(item: RollingProofOfDeliveryPreference): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryPreferenceLabelLengthRule(
    var maxLength: Int,
) : RollingProofOfDeliveryPreferenceRule {
    override fun check(item: RollingProofOfDeliveryPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryPreferenceCodeLengthRule(
    var maxLength: Int,
) : RollingProofOfDeliveryPreferenceRule {
    override fun check(item: RollingProofOfDeliveryPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryPreferenceCodePrefixRule(
    var prefix: String,
) : RollingProofOfDeliveryPreferenceRule {
    override fun check(item: RollingProofOfDeliveryPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling proof of delivery preference. */
data class RollingProofOfDeliveryPreferenceValidator(
    var rules: MutableList<RollingProofOfDeliveryPreferenceRule>,
) {
    fun validate(item: RollingProofOfDeliveryPreference): MutableList<String> {
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

    fun requireValid(item: RollingProofOfDeliveryPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingProofOfDeliveryPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingProofOfDeliveryPreferenceValidator(): RollingProofOfDeliveryPreferenceValidator {
    return RollingProofOfDeliveryPreferenceValidator(
        mutableListOf<RollingProofOfDeliveryPreferenceRule>(
            RollingProofOfDeliveryPreferenceFuelLitersRangeRule(5.0, 198.5),
            RollingProofOfDeliveryPreferenceRequiredCarrierRule(
                "rolling proof of delivery preference needs this field",
            ),
            RollingProofOfDeliveryPreferenceLabelLengthRule(120),
            RollingProofOfDeliveryPreferenceCodeLengthRule(64),
            RollingProofOfDeliveryPreferenceCodePrefixRule("REF"),
        ),
    )
}
