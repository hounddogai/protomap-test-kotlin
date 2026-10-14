package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.text.normalize

/** A rule a manual proof of delivery bundle must pass. Check returns its problems, or none. */
interface ManualProofOfDeliveryBundleRule {
    fun check(item: ManualProofOfDeliveryBundle): MutableList<String>
}

data class ManualProofOfDeliveryBundleRequiredCarrierRule(
    var message: String,
) : ManualProofOfDeliveryBundleRule {
    override fun check(item: ManualProofOfDeliveryBundle): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryBundleLabelLengthRule(var maxLength: Int) : ManualProofOfDeliveryBundleRule {
    override fun check(item: ManualProofOfDeliveryBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryBundleExternalRefLengthRule(
    var maxLength: Int,
) : ManualProofOfDeliveryBundleRule {
    override fun check(item: ManualProofOfDeliveryBundle): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryBundleRequiredExternalRefRule(
    var message: String,
) : ManualProofOfDeliveryBundleRule {
    override fun check(item: ManualProofOfDeliveryBundle): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryBundleMaximumRangeRule(
    var min: Double,
    var max: Double,
) : ManualProofOfDeliveryBundleRule {
    override fun check(item: ManualProofOfDeliveryBundle): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class ManualProofOfDeliveryBundleAllowedLabelRule(
    var allowed: MutableList<String>,
) : ManualProofOfDeliveryBundleRule {
    override fun check(item: ManualProofOfDeliveryBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual proof of delivery bundle. */
data class ManualProofOfDeliveryBundleValidator(var rules: MutableList<ManualProofOfDeliveryBundleRule>) {
    fun validate(item: ManualProofOfDeliveryBundle): MutableList<String> {
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

    fun requireValid(item: ManualProofOfDeliveryBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualProofOfDeliveryBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualProofOfDeliveryBundleValidator(): ManualProofOfDeliveryBundleValidator {
    return ManualProofOfDeliveryBundleValidator(
        mutableListOf<ManualProofOfDeliveryBundleRule>(
            ManualProofOfDeliveryBundleRequiredCarrierRule("manual proof of delivery bundle needs an entry"),
            ManualProofOfDeliveryBundleLabelLengthRule(16),
            ManualProofOfDeliveryBundleExternalRefLengthRule(255),
            ManualProofOfDeliveryBundleRequiredExternalRefRule(
                "manual proof of delivery bundle needs this field",
            ),
            ManualProofOfDeliveryBundleMaximumRangeRule(6.0, 287.4),
            ManualProofOfDeliveryBundleAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
