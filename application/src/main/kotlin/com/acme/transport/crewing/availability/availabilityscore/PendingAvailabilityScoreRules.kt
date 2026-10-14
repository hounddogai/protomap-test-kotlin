package com.acme.transport.crewing.availability.availabilityscore

/** A rule a pending availability score must pass. Check returns its problems, or none. */
interface PendingAvailabilityScoreRule {
    fun check(item: PendingAvailabilityScore): MutableList<String>
}

data class PendingAvailabilityScoreCodePrefixRule(var prefix: String) : PendingAvailabilityScoreRule {
    override fun check(item: PendingAvailabilityScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityScoreExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingAvailabilityScoreRule {
    override fun check(item: PendingAvailabilityScore): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityScoreRequiredCodeRule(var message: String) : PendingAvailabilityScoreRule {
    override fun check(item: PendingAvailabilityScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityScoreRequiredTitleRule(var message: String) : PendingAvailabilityScoreRule {
    override fun check(item: PendingAvailabilityScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityScoreShiftCodeLengthRule(var maxLength: Int) : PendingAvailabilityScoreRule {
    override fun check(item: PendingAvailabilityScore): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityScoreRequiredTrailerIdRule(var message: String) : PendingAvailabilityScoreRule {
    override fun check(item: PendingAvailabilityScore): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending availability score. */
data class PendingAvailabilityScoreValidator(var rules: MutableList<PendingAvailabilityScoreRule>) {
    fun validate(item: PendingAvailabilityScore): MutableList<String> {
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

    fun requireValid(item: PendingAvailabilityScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAvailabilityScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAvailabilityScoreValidator(): PendingAvailabilityScoreValidator {
    return PendingAvailabilityScoreValidator(
        mutableListOf<PendingAvailabilityScoreRule>(
            PendingAvailabilityScoreCodePrefixRule("Q"),
            PendingAvailabilityScoreExpiresAtRangeRule(16, 32),
            PendingAvailabilityScoreRequiredCodeRule("pending availability score needs this field"),
            PendingAvailabilityScoreRequiredTitleRule("pending availability score needs this field"),
            PendingAvailabilityScoreShiftCodeLengthRule(32),
            PendingAvailabilityScoreRequiredTrailerIdRule("pending availability score needs an entry"),
        ),
    )
}
