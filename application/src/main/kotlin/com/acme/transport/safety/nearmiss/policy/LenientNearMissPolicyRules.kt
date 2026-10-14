package com.acme.transport.safety.nearmiss.policy

/** A rule a lenient near miss policy must pass. Check returns its problems, or none. */
interface LenientNearMissPolicyRule {
    fun check(item: LenientNearMissPolicy): MutableList<String>
}

data class LenientNearMissPolicyRequiredNameRule(var message: String) : LenientNearMissPolicyRule {
    override fun check(item: LenientNearMissPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissPolicyRequiredCarrierRule(var message: String) : LenientNearMissPolicyRule {
    override fun check(item: LenientNearMissPolicy): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissPolicyNameLengthRule(var maxLength: Int) : LenientNearMissPolicyRule {
    override fun check(item: LenientNearMissPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissPolicyRequiredRegionRule(var message: String) : LenientNearMissPolicyRule {
    override fun check(item: LenientNearMissPolicy): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissPolicyCodePrefixRule(var prefix: String) : LenientNearMissPolicyRule {
    override fun check(item: LenientNearMissPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient near miss policy. */
data class LenientNearMissPolicyValidator(var rules: MutableList<LenientNearMissPolicyRule>) {
    fun validate(item: LenientNearMissPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientNearMissPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNearMissPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNearMissPolicyValidator(): LenientNearMissPolicyValidator {
    return LenientNearMissPolicyValidator(
        mutableListOf<LenientNearMissPolicyRule>(
            LenientNearMissPolicyRequiredNameRule("lenient near miss policy needs a value"),
            LenientNearMissPolicyRequiredCarrierRule("lenient near miss policy needs this field"),
            LenientNearMissPolicyNameLengthRule(86400),
            LenientNearMissPolicyRequiredRegionRule("lenient near miss policy needs this field"),
            LenientNearMissPolicyCodePrefixRule("ACM"),
        ),
    )
}
