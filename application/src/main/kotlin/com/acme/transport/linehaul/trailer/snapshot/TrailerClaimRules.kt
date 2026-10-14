package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a trailer claim must pass. Check returns its problems, or none. */
interface TrailerClaimRule {
    fun check(item: TrailerClaim): MutableList<String>
}

data class TrailerClaimChecksumLengthRule(var maxLength: Int) : TrailerClaimRule {
    override fun check(item: TrailerClaim): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerClaimCodePrefixRule(var prefix: String) : TrailerClaimRule {
    override fun check(item: TrailerClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerClaimCodeLengthRule(var maxLength: Int) : TrailerClaimRule {
    override fun check(item: TrailerClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerClaimAllowedTrailerIdRule(var allowed: MutableList<String>) : TrailerClaimRule {
    override fun check(item: TrailerClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer claim. */
data class TrailerClaimValidator(var rules: MutableList<TrailerClaimRule>) {
    fun validate(item: TrailerClaim): MutableList<String> {
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

    fun requireValid(item: TrailerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerClaimValidator(): TrailerClaimValidator {
    return TrailerClaimValidator(
        mutableListOf<TrailerClaimRule>(
            TrailerClaimChecksumLengthRule(1000),
            TrailerClaimCodePrefixRule("X"),
            TrailerClaimCodeLengthRule(86400),
            TrailerClaimAllowedTrailerIdRule(mutableListOf<String>("primary", "manual", "weighted")),
        ),
    )
}
