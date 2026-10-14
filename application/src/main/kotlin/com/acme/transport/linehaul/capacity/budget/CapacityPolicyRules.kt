package com.acme.transport.linehaul.capacity.budget

/** A rule a capacity policy must pass. Check returns its problems, or none. */
interface CapacityPolicyRule {
    fun check(item: CapacityPolicy): MutableList<String>
}

data class CapacityPolicyRequiredNameRule(var message: String) : CapacityPolicyRule {
    override fun check(item: CapacityPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityPolicyPalletsRangeRule(var min: Int, var max: Int) : CapacityPolicyRule {
    override fun check(item: CapacityPolicy): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class CapacityPolicyChannelLengthRule(var maxLength: Int) : CapacityPolicyRule {
    override fun check(item: CapacityPolicy): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityPolicyCodePrefixRule(var prefix: String) : CapacityPolicyRule {
    override fun check(item: CapacityPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity policy. */
data class CapacityPolicyValidator(var rules: MutableList<CapacityPolicyRule>) {
    fun validate(item: CapacityPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityPolicyValidator(): CapacityPolicyValidator {
    return CapacityPolicyValidator(
        mutableListOf<CapacityPolicyRule>(
            CapacityPolicyRequiredNameRule("capacity policy needs this field"),
            CapacityPolicyPalletsRangeRule(16, 80),
            CapacityPolicyChannelLengthRule(255),
            CapacityPolicyCodePrefixRule("OPS"),
        ),
    )
}
