package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.kit.text.normalize

/** A rule a lenient qualification claim must pass. Check returns its problems, or none. */
interface LenientQualificationClaimRule {
    fun check(item: LenientQualificationClaim): MutableList<String>
}

data class LenientQualificationClaimScoreRangeRule(
    var min: Double,
    var max: Double,
) : LenientQualificationClaimRule {
    override fun check(item: LenientQualificationClaim): MutableList<String> {
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

data class LenientQualificationClaimAllowedNameRule(
    var allowed: MutableList<String>,
) : LenientQualificationClaimRule {
    override fun check(item: LenientQualificationClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LenientQualificationClaimCodePrefixRule(var prefix: String) : LenientQualificationClaimRule {
    override fun check(item: LenientQualificationClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientQualificationClaimRequiredChannelRule(var message: String) : LenientQualificationClaimRule {
    override fun check(item: LenientQualificationClaim): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient qualification claim. */
data class LenientQualificationClaimValidator(var rules: MutableList<LenientQualificationClaimRule>) {
    fun validate(item: LenientQualificationClaim): MutableList<String> {
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

    fun requireValid(item: LenientQualificationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientQualificationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientQualificationClaimValidator(): LenientQualificationClaimValidator {
    return LenientQualificationClaimValidator(
        mutableListOf<LenientQualificationClaimRule>(
            LenientQualificationClaimScoreRangeRule(6.1, 755.0),
            LenientQualificationClaimAllowedNameRule(mutableListOf<String>("manual", "strict", "rolling")),
            LenientQualificationClaimCodePrefixRule("OPS"),
            LenientQualificationClaimRequiredChannelRule("lenient qualification claim needs a value"),
        ),
    )
}
