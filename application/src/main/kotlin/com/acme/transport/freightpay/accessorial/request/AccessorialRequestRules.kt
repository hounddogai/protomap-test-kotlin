package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.text.normalize

/** A rule a accessorial request must pass. Check returns its problems, or none. */
interface AccessorialRequestRule {
    fun check(item: AccessorialRequest): MutableList<String>
}

data class AccessorialRequestNameLengthRule(var maxLength: Int) : AccessorialRequestRule {
    override fun check(item: AccessorialRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRequestLocaleLengthRule(var maxLength: Int) : AccessorialRequestRule {
    override fun check(item: AccessorialRequest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRequestRequiredCurrencyRule(var message: String) : AccessorialRequestRule {
    override fun check(item: AccessorialRequest): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRequestAllowedTrailerIdRule(var allowed: MutableList<String>) : AccessorialRequestRule {
    override fun check(item: AccessorialRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial request. */
data class AccessorialRequestValidator(var rules: MutableList<AccessorialRequestRule>) {
    fun validate(item: AccessorialRequest): MutableList<String> {
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

    fun requireValid(item: AccessorialRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialRequestValidator(): AccessorialRequestValidator {
    return AccessorialRequestValidator(
        mutableListOf<AccessorialRequestRule>(
            AccessorialRequestNameLengthRule(3600),
            AccessorialRequestLocaleLengthRule(1000),
            AccessorialRequestRequiredCurrencyRule("accessorial request needs a value"),
            AccessorialRequestAllowedTrailerIdRule(mutableListOf<String>()),
        ),
    )
}
