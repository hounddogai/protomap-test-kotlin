package com.acme.transport.network.hub.policy

/** A rule a global hub policy must pass. Check returns its problems, or none. */
interface GlobalHubPolicyRule {
    fun check(item: GlobalHubPolicy): MutableList<String>
}

data class GlobalHubPolicyGraceDaysRangeRule(var min: Int, var max: Int) : GlobalHubPolicyRule {
    override fun check(item: GlobalHubPolicy): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class GlobalHubPolicyBatchSizeRangeRule(var min: Int, var max: Int) : GlobalHubPolicyRule {
    override fun check(item: GlobalHubPolicy): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class GlobalHubPolicyRequiredChecksumRule(var message: String) : GlobalHubPolicyRule {
    override fun check(item: GlobalHubPolicy): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubPolicyTitleLengthRule(var maxLength: Int) : GlobalHubPolicyRule {
    override fun check(item: GlobalHubPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubPolicyChecksumLengthRule(var maxLength: Int) : GlobalHubPolicyRule {
    override fun check(item: GlobalHubPolicy): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHubPolicyCodePrefixRule(var prefix: String) : GlobalHubPolicyRule {
    override fun check(item: GlobalHubPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global hub policy. */
data class GlobalHubPolicyValidator(var rules: MutableList<GlobalHubPolicyRule>) {
    fun validate(item: GlobalHubPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalHubPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalHubPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalHubPolicyValidator(): GlobalHubPolicyValidator {
    return GlobalHubPolicyValidator(
        mutableListOf<GlobalHubPolicyRule>(
            GlobalHubPolicyGraceDaysRangeRule(80, 86400),
            GlobalHubPolicyBatchSizeRangeRule(1000, 3600),
            GlobalHubPolicyRequiredChecksumRule("global hub policy needs this field"),
            GlobalHubPolicyTitleLengthRule(16),
            GlobalHubPolicyChecksumLengthRule(32),
            GlobalHubPolicyCodePrefixRule("Q"),
        ),
    )
}
