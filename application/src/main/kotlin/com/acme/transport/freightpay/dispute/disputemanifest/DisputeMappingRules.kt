package com.acme.transport.freightpay.dispute.disputemanifest

/** A rule a dispute mapping must pass. Check returns its problems, or none. */
interface DisputeMappingRule {
    fun check(item: DisputeMapping): MutableList<String>
}

data class DisputeMappingSegmentLengthRule(var maxLength: Int) : DisputeMappingRule {
    override fun check(item: DisputeMapping): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeMappingUnitLengthRule(var maxLength: Int) : DisputeMappingRule {
    override fun check(item: DisputeMapping): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeMappingRequiredCodeRule(var message: String) : DisputeMappingRule {
    override fun check(item: DisputeMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute mapping. */
data class DisputeMappingValidator(var rules: MutableList<DisputeMappingRule>) {
    fun validate(item: DisputeMapping): MutableList<String> {
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

    fun requireValid(item: DisputeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeMappingValidator(): DisputeMappingValidator {
    return DisputeMappingValidator(
        mutableListOf<DisputeMappingRule>(
            DisputeMappingSegmentLengthRule(32),
            DisputeMappingUnitLengthRule(64),
            DisputeMappingRequiredCodeRule("dispute mapping needs a value"),
        ),
    )
}
