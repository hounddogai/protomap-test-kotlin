package com.acme.transport.safety.cameraevent.eventledger

/** A rule a legacy camera event claim must pass. Check returns its problems, or none. */
interface LegacyCameraEventClaimRule {
    fun check(item: LegacyCameraEventClaim): MutableList<String>
}

data class LegacyCameraEventClaimRequiredNameRule(var message: String) : LegacyCameraEventClaimRule {
    override fun check(item: LegacyCameraEventClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventClaimCodeLengthRule(var maxLength: Int) : LegacyCameraEventClaimRule {
    override fun check(item: LegacyCameraEventClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventClaimScoreRangeRule(var min: Double, var max: Double) : LegacyCameraEventClaimRule {
    override fun check(item: LegacyCameraEventClaim): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class LegacyCameraEventClaimRequiredPhoneRule(var message: String) : LegacyCameraEventClaimRule {
    override fun check(item: LegacyCameraEventClaim): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyCameraEventClaimPhoneLengthRule(var maxLength: Int) : LegacyCameraEventClaimRule {
    override fun check(item: LegacyCameraEventClaim): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy camera event claim. */
data class LegacyCameraEventClaimValidator(var rules: MutableList<LegacyCameraEventClaimRule>) {
    fun validate(item: LegacyCameraEventClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCameraEventClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCameraEventClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCameraEventClaimValidator(): LegacyCameraEventClaimValidator {
    return LegacyCameraEventClaimValidator(
        mutableListOf<LegacyCameraEventClaimRule>(
            LegacyCameraEventClaimRequiredNameRule("legacy camera event claim needs an entry"),
            LegacyCameraEventClaimCodeLengthRule(255),
            LegacyCameraEventClaimScoreRangeRule(8.7, 1174.5),
            LegacyCameraEventClaimRequiredPhoneRule("legacy camera event claim needs an entry"),
            LegacyCameraEventClaimPhoneLengthRule(255),
        ),
    )
}
