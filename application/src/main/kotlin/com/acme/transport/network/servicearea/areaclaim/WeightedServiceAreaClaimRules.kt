package com.acme.transport.network.servicearea.areaclaim

/** A rule a weighted service area claim must pass. Check returns its problems, or none. */
interface WeightedServiceAreaClaimRule {
    fun check(item: WeightedServiceAreaClaim): MutableList<String>
}

data class WeightedServiceAreaClaimRequiredCodeRule(var message: String) : WeightedServiceAreaClaimRule {
    override fun check(item: WeightedServiceAreaClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaClaimCodePrefixRule(var prefix: String) : WeightedServiceAreaClaimRule {
    override fun check(item: WeightedServiceAreaClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaClaimRequiredNameRule(var message: String) : WeightedServiceAreaClaimRule {
    override fun check(item: WeightedServiceAreaClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaClaimExternalRefLengthRule(var maxLength: Int) : WeightedServiceAreaClaimRule {
    override fun check(item: WeightedServiceAreaClaim): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaClaimContactEmailLengthRule(var maxLength: Int) : WeightedServiceAreaClaimRule {
    override fun check(item: WeightedServiceAreaClaim): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted service area claim. */
data class WeightedServiceAreaClaimValidator(var rules: MutableList<WeightedServiceAreaClaimRule>) {
    fun validate(item: WeightedServiceAreaClaim): MutableList<String> {
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

    fun requireValid(item: WeightedServiceAreaClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedServiceAreaClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedServiceAreaClaimValidator(): WeightedServiceAreaClaimValidator {
    return WeightedServiceAreaClaimValidator(
        mutableListOf<WeightedServiceAreaClaimRule>(
            WeightedServiceAreaClaimRequiredCodeRule("weighted service area claim needs a value"),
            WeightedServiceAreaClaimCodePrefixRule("Q"),
            WeightedServiceAreaClaimRequiredNameRule("weighted service area claim needs an entry"),
            WeightedServiceAreaClaimExternalRefLengthRule(64),
            WeightedServiceAreaClaimContactEmailLengthRule(16),
        ),
    )
}
