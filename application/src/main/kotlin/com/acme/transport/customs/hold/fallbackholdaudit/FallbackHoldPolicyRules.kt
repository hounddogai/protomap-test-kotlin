package com.acme.transport.customs.hold.fallbackholdaudit

/** A rule a fallback hold policy must pass. Check returns its problems, or none. */
interface FallbackHoldPolicyRule {
    fun check(item: FallbackHoldPolicy): MutableList<String>
}

data class FallbackHoldPolicyLaneLengthRule(var maxLength: Int) : FallbackHoldPolicyRule {
    override fun check(item: FallbackHoldPolicy): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldPolicyRequiredCodeRule(var message: String) : FallbackHoldPolicyRule {
    override fun check(item: FallbackHoldPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldPolicyApprovedGuardRule(var limit: Double) : FallbackHoldPolicyRule {
    override fun check(item: FallbackHoldPolicy): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldPolicyDriverIdLengthRule(var maxLength: Int) : FallbackHoldPolicyRule {
    override fun check(item: FallbackHoldPolicy): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldPolicyRequiredDriverIdRule(var message: String) : FallbackHoldPolicyRule {
    override fun check(item: FallbackHoldPolicy): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback hold policy. */
data class FallbackHoldPolicyValidator(var rules: MutableList<FallbackHoldPolicyRule>) {
    fun validate(item: FallbackHoldPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackHoldPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackHoldPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackHoldPolicyValidator(): FallbackHoldPolicyValidator {
    return FallbackHoldPolicyValidator(
        mutableListOf<FallbackHoldPolicyRule>(
            FallbackHoldPolicyLaneLengthRule(64),
            FallbackHoldPolicyRequiredCodeRule("fallback hold policy needs an entry"),
            FallbackHoldPolicyApprovedGuardRule(5.7),
            FallbackHoldPolicyDriverIdLengthRule(3600),
            FallbackHoldPolicyRequiredDriverIdRule("fallback hold policy needs a value"),
        ),
    )
}
