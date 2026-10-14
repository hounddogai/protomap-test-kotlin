package com.acme.transport.lastmile.parcel.cycle

/** A rule a default parcel milestone must pass. Check returns its problems, or none. */
interface DefaultParcelMilestoneRule {
    fun check(item: DefaultParcelMilestone): MutableList<String>
}

data class DefaultParcelMilestoneCodeLengthRule(var maxLength: Int) : DefaultParcelMilestoneRule {
    override fun check(item: DefaultParcelMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelMilestoneContactEmailLengthRule(var maxLength: Int) : DefaultParcelMilestoneRule {
    override fun check(item: DefaultParcelMilestone): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelMilestoneRequiredContactEmailRule(var message: String) : DefaultParcelMilestoneRule {
    override fun check(item: DefaultParcelMilestone): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelMilestoneRetriesRangeRule(var min: Int, var max: Int) : DefaultParcelMilestoneRule {
    override fun check(item: DefaultParcelMilestone): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelMilestoneCodePrefixRule(var prefix: String) : DefaultParcelMilestoneRule {
    override fun check(item: DefaultParcelMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelMilestoneExpiresAtRangeRule(var min: Int, var max: Int) : DefaultParcelMilestoneRule {
    override fun check(item: DefaultParcelMilestone): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default parcel milestone. */
data class DefaultParcelMilestoneValidator(var rules: MutableList<DefaultParcelMilestoneRule>) {
    fun validate(item: DefaultParcelMilestone): MutableList<String> {
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

    fun requireValid(item: DefaultParcelMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultParcelMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultParcelMilestoneValidator(): DefaultParcelMilestoneValidator {
    return DefaultParcelMilestoneValidator(
        mutableListOf<DefaultParcelMilestoneRule>(
            DefaultParcelMilestoneCodeLengthRule(3600),
            DefaultParcelMilestoneContactEmailLengthRule(3600),
            DefaultParcelMilestoneRequiredContactEmailRule("default parcel milestone needs this field"),
            DefaultParcelMilestoneRetriesRangeRule(3600, 86400),
            DefaultParcelMilestoneCodePrefixRule("ACM"),
            DefaultParcelMilestoneExpiresAtRangeRule(255, 86400),
        ),
    )
}
