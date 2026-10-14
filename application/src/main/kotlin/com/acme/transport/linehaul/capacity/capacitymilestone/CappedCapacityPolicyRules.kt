package com.acme.transport.linehaul.capacity.capacitymilestone

/** A rule a capped capacity policy must pass. Check returns its problems, or none. */
interface CappedCapacityPolicyRule {
    fun check(item: CappedCapacityPolicy): MutableList<String>
}

data class CappedCapacityPolicyRequiredCodeRule(var message: String) : CappedCapacityPolicyRule {
    override fun check(item: CappedCapacityPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityPolicyRequiredNameRule(var message: String) : CappedCapacityPolicyRule {
    override fun check(item: CappedCapacityPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityPolicyNameLengthRule(var maxLength: Int) : CappedCapacityPolicyRule {
    override fun check(item: CappedCapacityPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityPolicyPinnedGuardRule(var limit: Double) : CappedCapacityPolicyRule {
    override fun check(item: CappedCapacityPolicy): MutableList<String> {
        if (item.pinned && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class CappedCapacityPolicyRateRangeRule(var min: Double, var max: Double) : CappedCapacityPolicyRule {
    override fun check(item: CappedCapacityPolicy): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped capacity policy. */
data class CappedCapacityPolicyValidator(var rules: MutableList<CappedCapacityPolicyRule>) {
    fun validate(item: CappedCapacityPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCapacityPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCapacityPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCapacityPolicyValidator(): CappedCapacityPolicyValidator {
    return CappedCapacityPolicyValidator(
        mutableListOf<CappedCapacityPolicyRule>(
            CappedCapacityPolicyRequiredCodeRule("capped capacity policy needs a value"),
            CappedCapacityPolicyRequiredNameRule("capped capacity policy needs this field"),
            CappedCapacityPolicyNameLengthRule(86400),
            CappedCapacityPolicyPinnedGuardRule(0.8),
            CappedCapacityPolicyRateRangeRule(5.1, 4147.4),
        ),
    )
}
