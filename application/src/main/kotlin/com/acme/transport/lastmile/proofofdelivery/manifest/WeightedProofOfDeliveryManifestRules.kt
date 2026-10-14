package com.acme.transport.lastmile.proofofdelivery.manifest

/** A rule a weighted proof of delivery manifest must pass. Check returns its problems, or none. */
interface WeightedProofOfDeliveryManifestRule {
    fun check(item: WeightedProofOfDeliveryManifest): MutableList<String>
}

data class WeightedProofOfDeliveryManifestDockLengthRule(
    var maxLength: Int,
) : WeightedProofOfDeliveryManifestRule {
    override fun check(item: WeightedProofOfDeliveryManifest): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryManifestRequiredCodeRule(
    var message: String,
) : WeightedProofOfDeliveryManifestRule {
    override fun check(item: WeightedProofOfDeliveryManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryManifestReviewerLengthRule(
    var maxLength: Int,
) : WeightedProofOfDeliveryManifestRule {
    override fun check(item: WeightedProofOfDeliveryManifest): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryManifestCodePrefixRule(
    var prefix: String,
) : WeightedProofOfDeliveryManifestRule {
    override fun check(item: WeightedProofOfDeliveryManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted proof of delivery manifest. */
data class WeightedProofOfDeliveryManifestValidator(
    var rules: MutableList<WeightedProofOfDeliveryManifestRule>,
) {
    fun validate(item: WeightedProofOfDeliveryManifest): MutableList<String> {
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

    fun requireValid(item: WeightedProofOfDeliveryManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedProofOfDeliveryManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedProofOfDeliveryManifestValidator(): WeightedProofOfDeliveryManifestValidator {
    return WeightedProofOfDeliveryManifestValidator(
        mutableListOf<WeightedProofOfDeliveryManifestRule>(
            WeightedProofOfDeliveryManifestDockLengthRule(32),
            WeightedProofOfDeliveryManifestRequiredCodeRule("weighted proof of delivery manifest needs a value"),
            WeightedProofOfDeliveryManifestReviewerLengthRule(3600),
            WeightedProofOfDeliveryManifestCodePrefixRule("REF"),
        ),
    )
}
