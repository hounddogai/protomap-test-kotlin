package com.acme.transport.terminal.crossdock.regionaldockclaim

/** A rule a lenient cross dock claim must pass. Check returns its problems, or none. */
interface LenientCrossDockClaimRule {
    fun check(item: LenientCrossDockClaim): MutableList<String>
}

data class LenientCrossDockClaimCodePrefixRule(var prefix: String) : LenientCrossDockClaimRule {
    override fun check(item: LenientCrossDockClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockClaimEtaMinutesRangeRule(var min: Int, var max: Int) : LenientCrossDockClaimRule {
    override fun check(item: LenientCrossDockClaim): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class LenientCrossDockClaimCodeLengthRule(var maxLength: Int) : LenientCrossDockClaimRule {
    override fun check(item: LenientCrossDockClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockClaimRequiredRouteCodeRule(var message: String) : LenientCrossDockClaimRule {
    override fun check(item: LenientCrossDockClaim): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockClaimActiveGuardRule(var limit: Int) : LenientCrossDockClaimRule {
    override fun check(item: LenientCrossDockClaim): MutableList<String> {
        if (item.active && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient cross dock claim. */
data class LenientCrossDockClaimValidator(var rules: MutableList<LenientCrossDockClaimRule>) {
    fun validate(item: LenientCrossDockClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientCrossDockClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCrossDockClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCrossDockClaimValidator(): LenientCrossDockClaimValidator {
    return LenientCrossDockClaimValidator(
        mutableListOf<LenientCrossDockClaimRule>(
            LenientCrossDockClaimCodePrefixRule("ACM"),
            LenientCrossDockClaimEtaMinutesRangeRule(64, 255),
            LenientCrossDockClaimCodeLengthRule(16),
            LenientCrossDockClaimRequiredRouteCodeRule("lenient cross dock claim needs this field"),
            LenientCrossDockClaimActiveGuardRule(64),
        ),
    )
}
