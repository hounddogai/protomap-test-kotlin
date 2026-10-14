package com.acme.transport.crewing.availability.request

import com.acme.transport.kit.text.normalize

/** A rule a availability target must pass. Check returns its problems, or none. */
interface AvailabilityTargetRule {
    fun check(item: AvailabilityTarget): MutableList<String>
}

data class AvailabilityTargetRequiredNameRule(var message: String) : AvailabilityTargetRule {
    override fun check(item: AvailabilityTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityTargetNameLengthRule(var maxLength: Int) : AvailabilityTargetRule {
    override fun check(item: AvailabilityTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityTargetAllowedNameRule(var allowed: MutableList<String>) : AvailabilityTargetRule {
    override fun check(item: AvailabilityTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityTargetCodePrefixRule(var prefix: String) : AvailabilityTargetRule {
    override fun check(item: AvailabilityTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability target. */
data class AvailabilityTargetValidator(var rules: MutableList<AvailabilityTargetRule>) {
    fun validate(item: AvailabilityTarget): MutableList<String> {
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

    fun requireValid(item: AvailabilityTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityTargetValidator(): AvailabilityTargetValidator {
    return AvailabilityTargetValidator(
        mutableListOf<AvailabilityTargetRule>(
            AvailabilityTargetRequiredNameRule("availability target needs an entry"),
            AvailabilityTargetNameLengthRule(120),
            AvailabilityTargetAllowedNameRule(mutableListOf<String>("primary", "weighted")),
            AvailabilityTargetCodePrefixRule("ACM"),
        ),
    )
}
