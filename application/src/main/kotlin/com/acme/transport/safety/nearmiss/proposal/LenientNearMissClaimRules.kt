package com.acme.transport.safety.nearmiss.proposal

/** A rule a lenient near miss claim must pass. Check returns its problems, or none. */
interface LenientNearMissClaimRule {
    fun check(item: LenientNearMissClaim): MutableList<String>
}

data class LenientNearMissClaimQuantityRangeRule(var min: Int, var max: Int) : LenientNearMissClaimRule {
    override fun check(item: LenientNearMissClaim): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class LenientNearMissClaimRequiredCodeRule(var message: String) : LenientNearMissClaimRule {
    override fun check(item: LenientNearMissClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissClaimLabelLengthRule(var maxLength: Int) : LenientNearMissClaimRule {
    override fun check(item: LenientNearMissClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissClaimTitleLengthRule(var maxLength: Int) : LenientNearMissClaimRule {
    override fun check(item: LenientNearMissClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientNearMissClaimRequiredChecksumRule(var message: String) : LenientNearMissClaimRule {
    override fun check(item: LenientNearMissClaim): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient near miss claim. */
data class LenientNearMissClaimValidator(var rules: MutableList<LenientNearMissClaimRule>) {
    fun validate(item: LenientNearMissClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientNearMissClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNearMissClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNearMissClaimValidator(): LenientNearMissClaimValidator {
    return LenientNearMissClaimValidator(
        mutableListOf<LenientNearMissClaimRule>(
            LenientNearMissClaimQuantityRangeRule(120, 86400),
            LenientNearMissClaimRequiredCodeRule("lenient near miss claim needs this field"),
            LenientNearMissClaimLabelLengthRule(120),
            LenientNearMissClaimTitleLengthRule(86400),
            LenientNearMissClaimRequiredChecksumRule("lenient near miss claim needs an entry"),
        ),
    )
}
