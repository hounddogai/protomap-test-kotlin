package com.acme.transport.lastmile.proofofdelivery.review

/** A rule a capped proof of delivery profile must pass. Check returns its problems, or none. */
interface CappedProofOfDeliveryProfileRule {
    fun check(item: CappedProofOfDeliveryProfile): MutableList<String>
}

data class CappedProofOfDeliveryProfileCodePrefixRule(var prefix: String) : CappedProofOfDeliveryProfileRule {
    override fun check(item: CappedProofOfDeliveryProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedProofOfDeliveryProfileRequiredCodeRule(
    var message: String,
) : CappedProofOfDeliveryProfileRule {
    override fun check(item: CappedProofOfDeliveryProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedProofOfDeliveryProfileReviewerLengthRule(
    var maxLength: Int,
) : CappedProofOfDeliveryProfileRule {
    override fun check(item: CappedProofOfDeliveryProfile): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedProofOfDeliveryProfileNameLengthRule(var maxLength: Int) : CappedProofOfDeliveryProfileRule {
    override fun check(item: CappedProofOfDeliveryProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedProofOfDeliveryProfileCapacityRangeRule(
    var min: Int,
    var max: Int,
) : CappedProofOfDeliveryProfileRule {
    override fun check(item: CappedProofOfDeliveryProfile): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped proof of delivery profile. */
data class CappedProofOfDeliveryProfileValidator(var rules: MutableList<CappedProofOfDeliveryProfileRule>) {
    fun validate(item: CappedProofOfDeliveryProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedProofOfDeliveryProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedProofOfDeliveryProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedProofOfDeliveryProfileValidator(): CappedProofOfDeliveryProfileValidator {
    return CappedProofOfDeliveryProfileValidator(
        mutableListOf<CappedProofOfDeliveryProfileRule>(
            CappedProofOfDeliveryProfileCodePrefixRule("ACM"),
            CappedProofOfDeliveryProfileRequiredCodeRule("capped proof of delivery profile needs an entry"),
            CappedProofOfDeliveryProfileReviewerLengthRule(1000),
            CappedProofOfDeliveryProfileNameLengthRule(120),
            CappedProofOfDeliveryProfileCapacityRangeRule(16, 120),
        ),
    )
}
