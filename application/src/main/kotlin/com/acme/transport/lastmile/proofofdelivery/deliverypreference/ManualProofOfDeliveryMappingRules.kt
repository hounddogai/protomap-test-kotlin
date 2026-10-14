package com.acme.transport.lastmile.proofofdelivery.deliverypreference

/** A rule a manual proof of delivery mapping must pass. Check returns its problems, or none. */
interface ManualProofOfDeliveryMappingRule {
    fun check(item: ManualProofOfDeliveryMapping): MutableList<String>
}

data class ManualProofOfDeliveryMappingRequiredCodeRule(
    var message: String,
) : ManualProofOfDeliveryMappingRule {
    override fun check(item: ManualProofOfDeliveryMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryMappingLabelLengthRule(var maxLength: Int) : ManualProofOfDeliveryMappingRule {
    override fun check(item: ManualProofOfDeliveryMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryMappingRequiredLabelRule(
    var message: String,
) : ManualProofOfDeliveryMappingRule {
    override fun check(item: ManualProofOfDeliveryMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual proof of delivery mapping. */
data class ManualProofOfDeliveryMappingValidator(var rules: MutableList<ManualProofOfDeliveryMappingRule>) {
    fun validate(item: ManualProofOfDeliveryMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualProofOfDeliveryMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualProofOfDeliveryMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultManualProofOfDeliveryMappingValidator(): ManualProofOfDeliveryMappingValidator {
    return ManualProofOfDeliveryMappingValidator(
        mutableListOf<ManualProofOfDeliveryMappingRule>(
            ManualProofOfDeliveryMappingRequiredCodeRule("manual proof of delivery mapping needs an entry"),
            ManualProofOfDeliveryMappingLabelLengthRule(3600),
            ManualProofOfDeliveryMappingRequiredLabelRule("manual proof of delivery mapping needs a value"),
        ),
    )
}
