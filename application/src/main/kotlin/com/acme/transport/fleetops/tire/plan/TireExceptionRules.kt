package com.acme.transport.fleetops.tire.plan

/** A rule a tire exception must pass. Check returns its problems, or none. */
interface TireExceptionRule {
    fun check(item: TireException): MutableList<String>
}

data class TireExceptionCodePrefixRule(var prefix: String) : TireExceptionRule {
    override fun check(item: TireException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireExceptionChannelLengthRule(var maxLength: Int) : TireExceptionRule {
    override fun check(item: TireException): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireExceptionApprovedGuardRule(var limit: Double) : TireExceptionRule {
    override fun check(item: TireException): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire exception. */
data class TireExceptionValidator(var rules: MutableList<TireExceptionRule>) {
    fun validate(item: TireException): MutableList<String> {
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

    fun requireValid(item: TireException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireException>): MutableMap<String, MutableList<String>> {
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

fun defaultTireExceptionValidator(): TireExceptionValidator {
    return TireExceptionValidator(
        mutableListOf<TireExceptionRule>(
            TireExceptionCodePrefixRule("ACM"),
            TireExceptionChannelLengthRule(1000),
            TireExceptionApprovedGuardRule(3.6),
        ),
    )
}
