package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.normalize

/** A rule a locker mapping must pass. Check returns its problems, or none. */
interface LockerMappingRule {
    fun check(item: LockerMapping): MutableList<String>
}

data class LockerMappingRequiredExternalRefRule(var message: String) : LockerMappingRule {
    override fun check(item: LockerMapping): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerMappingExternalRefLengthRule(var maxLength: Int) : LockerMappingRule {
    override fun check(item: LockerMapping): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerMappingCreatedAtRangeRule(var min: Int, var max: Int) : LockerMappingRule {
    override fun check(item: LockerMapping): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class LockerMappingRequiredCodeRule(var message: String) : LockerMappingRule {
    override fun check(item: LockerMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerMappingAllowedExternalRefRule(var allowed: MutableList<String>) : LockerMappingRule {
    override fun check(item: LockerMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker mapping. */
data class LockerMappingValidator(var rules: MutableList<LockerMappingRule>) {
    fun validate(item: LockerMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerMappingValidator(): LockerMappingValidator {
    return LockerMappingValidator(
        mutableListOf<LockerMappingRule>(
            LockerMappingRequiredExternalRefRule("locker mapping needs a value"),
            LockerMappingExternalRefLengthRule(32),
            LockerMappingCreatedAtRangeRule(80, 255),
            LockerMappingRequiredCodeRule("locker mapping needs a value"),
            LockerMappingAllowedExternalRefRule(mutableListOf<String>("pending", "regional")),
        ),
    )
}
