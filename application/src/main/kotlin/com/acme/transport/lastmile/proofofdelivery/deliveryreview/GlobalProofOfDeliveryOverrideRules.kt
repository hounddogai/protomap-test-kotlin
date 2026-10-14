package com.acme.transport.lastmile.proofofdelivery.deliveryreview

/** A rule a global proof of delivery override must pass. Check returns its problems, or none. */
interface GlobalProofOfDeliveryOverrideRule {
    fun check(item: GlobalProofOfDeliveryOverride): MutableList<String>
}

data class GlobalProofOfDeliveryOverrideScoreRangeRule(
    var min: Double,
    var max: Double,
) : GlobalProofOfDeliveryOverrideRule {
    override fun check(item: GlobalProofOfDeliveryOverride): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryOverrideRequiredNameRule(
    var message: String,
) : GlobalProofOfDeliveryOverrideRule {
    override fun check(item: GlobalProofOfDeliveryOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryOverrideRequiredCodeRule(
    var message: String,
) : GlobalProofOfDeliveryOverrideRule {
    override fun check(item: GlobalProofOfDeliveryOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryOverrideCodeLengthRule(var maxLength: Int) : GlobalProofOfDeliveryOverrideRule {
    override fun check(item: GlobalProofOfDeliveryOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryOverrideCodePrefixRule(var prefix: String) : GlobalProofOfDeliveryOverrideRule {
    override fun check(item: GlobalProofOfDeliveryOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryOverrideNameLengthRule(var maxLength: Int) : GlobalProofOfDeliveryOverrideRule {
    override fun check(item: GlobalProofOfDeliveryOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global proof of delivery override. */
data class GlobalProofOfDeliveryOverrideValidator(var rules: MutableList<GlobalProofOfDeliveryOverrideRule>) {
    fun validate(item: GlobalProofOfDeliveryOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalProofOfDeliveryOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalProofOfDeliveryOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalProofOfDeliveryOverrideValidator(): GlobalProofOfDeliveryOverrideValidator {
    return GlobalProofOfDeliveryOverrideValidator(
        mutableListOf<GlobalProofOfDeliveryOverrideRule>(
            GlobalProofOfDeliveryOverrideScoreRangeRule(4.4, 4651.2),
            GlobalProofOfDeliveryOverrideRequiredNameRule("global proof of delivery override needs a value"),
            GlobalProofOfDeliveryOverrideRequiredCodeRule("global proof of delivery override needs this field"),
            GlobalProofOfDeliveryOverrideCodeLengthRule(32),
            GlobalProofOfDeliveryOverrideCodePrefixRule("OPS"),
            GlobalProofOfDeliveryOverrideNameLengthRule(16),
        ),
    )
}
