package com.acme.transport.customs.broker.exception

import com.acme.transport.kit.text.normalize

/** A rule a broker exception must pass. Check returns its problems, or none. */
interface BrokerExceptionRule {
    fun check(item: BrokerException): MutableList<String>
}

data class BrokerExceptionCodePrefixRule(var prefix: String) : BrokerExceptionRule {
    override fun check(item: BrokerException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerExceptionNameLengthRule(var maxLength: Int) : BrokerExceptionRule {
    override fun check(item: BrokerException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerExceptionApprovedGuardRule(var limit: Int) : BrokerExceptionRule {
    override fun check(item: BrokerException): MutableList<String> {
        if (item.approved && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class BrokerExceptionRequiredSegmentRule(var message: String) : BrokerExceptionRule {
    override fun check(item: BrokerException): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerExceptionPalletsRangeRule(var min: Int, var max: Int) : BrokerExceptionRule {
    override fun check(item: BrokerException): MutableList<String> {
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

data class BrokerExceptionAllowedNameRule(var allowed: MutableList<String>) : BrokerExceptionRule {
    override fun check(item: BrokerException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker exception. */
data class BrokerExceptionValidator(var rules: MutableList<BrokerExceptionRule>) {
    fun validate(item: BrokerException): MutableList<String> {
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

    fun requireValid(item: BrokerException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerException>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerExceptionValidator(): BrokerExceptionValidator {
    return BrokerExceptionValidator(
        mutableListOf<BrokerExceptionRule>(
            BrokerExceptionCodePrefixRule("REF"),
            BrokerExceptionNameLengthRule(86400),
            BrokerExceptionApprovedGuardRule(86400),
            BrokerExceptionRequiredSegmentRule("broker exception needs a value"),
            BrokerExceptionPalletsRangeRule(16, 120),
            BrokerExceptionAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
