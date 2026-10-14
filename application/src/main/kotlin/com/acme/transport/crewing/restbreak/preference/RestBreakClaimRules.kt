package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.kit.text.normalize

/** A rule a rest break claim must pass. Check returns its problems, or none. */
interface RestBreakClaimRule {
    fun check(item: RestBreakClaim): MutableList<String>
}

data class RestBreakClaimCodePrefixRule(var prefix: String) : RestBreakClaimRule {
    override fun check(item: RestBreakClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakClaimChannelLengthRule(var maxLength: Int) : RestBreakClaimRule {
    override fun check(item: RestBreakClaim): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakClaimRequiredCodeRule(var message: String) : RestBreakClaimRule {
    override fun check(item: RestBreakClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakClaimAllowedChannelRule(var allowed: MutableList<String>) : RestBreakClaimRule {
    override fun check(item: RestBreakClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakClaimTimezoneLengthRule(var maxLength: Int) : RestBreakClaimRule {
    override fun check(item: RestBreakClaim): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakClaimRequiredLabelRule(var message: String) : RestBreakClaimRule {
    override fun check(item: RestBreakClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break claim. */
data class RestBreakClaimValidator(var rules: MutableList<RestBreakClaimRule>) {
    fun validate(item: RestBreakClaim): MutableList<String> {
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

    fun requireValid(item: RestBreakClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakClaimValidator(): RestBreakClaimValidator {
    return RestBreakClaimValidator(
        mutableListOf<RestBreakClaimRule>(
            RestBreakClaimCodePrefixRule("Q"),
            RestBreakClaimChannelLengthRule(120),
            RestBreakClaimRequiredCodeRule("rest break claim needs this field"),
            RestBreakClaimAllowedChannelRule(mutableListOf<String>("pending", "weighted")),
            RestBreakClaimTimezoneLengthRule(80),
            RestBreakClaimRequiredLabelRule("rest break claim needs an entry"),
        ),
    )
}
