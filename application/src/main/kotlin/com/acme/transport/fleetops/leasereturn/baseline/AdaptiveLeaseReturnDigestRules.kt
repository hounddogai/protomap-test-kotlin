package com.acme.transport.fleetops.leasereturn.baseline

/** A rule a adaptive lease return digest must pass. Check returns its problems, or none. */
interface AdaptiveLeaseReturnDigestRule {
    fun check(item: AdaptiveLeaseReturnDigest): MutableList<String>
}

data class AdaptiveLeaseReturnDigestCodePrefixRule(var prefix: String) : AdaptiveLeaseReturnDigestRule {
    override fun check(item: AdaptiveLeaseReturnDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnDigestLabelLengthRule(var maxLength: Int) : AdaptiveLeaseReturnDigestRule {
    override fun check(item: AdaptiveLeaseReturnDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnDigestRequiredLabelRule(var message: String) : AdaptiveLeaseReturnDigestRule {
    override fun check(item: AdaptiveLeaseReturnDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnDigestRequiredCodeRule(var message: String) : AdaptiveLeaseReturnDigestRule {
    override fun check(item: AdaptiveLeaseReturnDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLeaseReturnDigestCodeLengthRule(var maxLength: Int) : AdaptiveLeaseReturnDigestRule {
    override fun check(item: AdaptiveLeaseReturnDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive lease return digest. */
data class AdaptiveLeaseReturnDigestValidator(var rules: MutableList<AdaptiveLeaseReturnDigestRule>) {
    fun validate(item: AdaptiveLeaseReturnDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLeaseReturnDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLeaseReturnDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLeaseReturnDigestValidator(): AdaptiveLeaseReturnDigestValidator {
    return AdaptiveLeaseReturnDigestValidator(
        mutableListOf<AdaptiveLeaseReturnDigestRule>(
            AdaptiveLeaseReturnDigestCodePrefixRule("OPS"),
            AdaptiveLeaseReturnDigestLabelLengthRule(120),
            AdaptiveLeaseReturnDigestRequiredLabelRule("adaptive lease return digest needs this field"),
            AdaptiveLeaseReturnDigestRequiredCodeRule("adaptive lease return digest needs this field"),
            AdaptiveLeaseReturnDigestCodeLengthRule(32),
        ),
    )
}
