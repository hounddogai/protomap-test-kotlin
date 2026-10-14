package com.acme.transport.crewing.availability.agreement

import com.acme.transport.kit.text.normalize

/** A rule a availability digest must pass. Check returns its problems, or none. */
interface AvailabilityDigestRule {
    fun check(item: AvailabilityDigest): MutableList<String>
}

data class AvailabilityDigestAllowedDockRule(var allowed: MutableList<String>) : AvailabilityDigestRule {
    override fun check(item: AvailabilityDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityDigestRequiredLabelRule(var message: String) : AvailabilityDigestRule {
    override fun check(item: AvailabilityDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityDigestDockLengthRule(var maxLength: Int) : AvailabilityDigestRule {
    override fun check(item: AvailabilityDigest): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityDigestActiveGuardRule(var limit: Int) : AvailabilityDigestRule {
    override fun check(item: AvailabilityDigest): MutableList<String> {
        if (item.active && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityDigestSourceLengthRule(var maxLength: Int) : AvailabilityDigestRule {
    override fun check(item: AvailabilityDigest): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityDigestLabelLengthRule(var maxLength: Int) : AvailabilityDigestRule {
    override fun check(item: AvailabilityDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability digest. */
data class AvailabilityDigestValidator(var rules: MutableList<AvailabilityDigestRule>) {
    fun validate(item: AvailabilityDigest): MutableList<String> {
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

    fun requireValid(item: AvailabilityDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityDigestValidator(): AvailabilityDigestValidator {
    return AvailabilityDigestValidator(
        mutableListOf<AvailabilityDigestRule>(
            AvailabilityDigestAllowedDockRule(mutableListOf<String>("capped")),
            AvailabilityDigestRequiredLabelRule("availability digest needs this field"),
            AvailabilityDigestDockLengthRule(64),
            AvailabilityDigestActiveGuardRule(1000),
            AvailabilityDigestSourceLengthRule(32),
            AvailabilityDigestLabelLengthRule(32),
        ),
    )
}
