package com.acme.transport.lastmile.redelivery.target

/** A rule a redelivery group must pass. Check returns its problems, or none. */
interface RedeliveryGroupRule {
    fun check(item: RedeliveryGroup): MutableList<String>
}

data class RedeliveryGroupRequiredCodeRule(var message: String) : RedeliveryGroupRule {
    override fun check(item: RedeliveryGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryGroupLabelLengthRule(var maxLength: Int) : RedeliveryGroupRule {
    override fun check(item: RedeliveryGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryGroupRequiredLabelRule(var message: String) : RedeliveryGroupRule {
    override fun check(item: RedeliveryGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryGroupCodePrefixRule(var prefix: String) : RedeliveryGroupRule {
    override fun check(item: RedeliveryGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryGroupTrailerIdLengthRule(var maxLength: Int) : RedeliveryGroupRule {
    override fun check(item: RedeliveryGroup): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery group. */
data class RedeliveryGroupValidator(var rules: MutableList<RedeliveryGroupRule>) {
    fun validate(item: RedeliveryGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryGroupValidator(): RedeliveryGroupValidator {
    return RedeliveryGroupValidator(
        mutableListOf<RedeliveryGroupRule>(
            RedeliveryGroupRequiredCodeRule("redelivery group needs an entry"),
            RedeliveryGroupLabelLengthRule(16),
            RedeliveryGroupRequiredLabelRule("redelivery group needs this field"),
            RedeliveryGroupCodePrefixRule("OPS"),
            RedeliveryGroupTrailerIdLengthRule(80),
        ),
    )
}
