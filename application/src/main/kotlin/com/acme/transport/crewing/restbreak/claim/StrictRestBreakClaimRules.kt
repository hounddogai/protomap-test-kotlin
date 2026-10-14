package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.text.normalize

/** A rule a strict rest break claim must pass. Check returns its problems, or none. */
interface StrictRestBreakClaimRule {
    fun check(item: StrictRestBreakClaim): MutableList<String>
}

data class StrictRestBreakClaimAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : StrictRestBreakClaimRule {
    override fun check(item: StrictRestBreakClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is too long")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakClaimRequiredTrailerIdRule(var message: String) : StrictRestBreakClaimRule {
    override fun check(item: StrictRestBreakClaim): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakClaimDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : StrictRestBreakClaimRule {
    override fun check(item: StrictRestBreakClaim): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class StrictRestBreakClaimDriverIdLengthRule(var maxLength: Int) : StrictRestBreakClaimRule {
    override fun check(item: StrictRestBreakClaim): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakClaimRequiredCodeRule(var message: String) : StrictRestBreakClaimRule {
    override fun check(item: StrictRestBreakClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rest break claim. */
data class StrictRestBreakClaimValidator(var rules: MutableList<StrictRestBreakClaimRule>) {
    fun validate(item: StrictRestBreakClaim): MutableList<String> {
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

    fun requireValid(item: StrictRestBreakClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRestBreakClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRestBreakClaimValidator(): StrictRestBreakClaimValidator {
    return StrictRestBreakClaimValidator(
        mutableListOf<StrictRestBreakClaimRule>(
            StrictRestBreakClaimAllowedTrailerIdRule(mutableListOf<String>("scheduled")),
            StrictRestBreakClaimRequiredTrailerIdRule("strict rest break claim needs this field"),
            StrictRestBreakClaimDistanceKmRangeRule(8.7, 4604.6),
            StrictRestBreakClaimDriverIdLengthRule(1000),
            StrictRestBreakClaimRequiredCodeRule("strict rest break claim needs an entry"),
        ),
    )
}
