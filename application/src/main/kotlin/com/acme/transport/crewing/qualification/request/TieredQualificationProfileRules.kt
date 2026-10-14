package com.acme.transport.crewing.qualification.request

/** A rule a tiered qualification profile must pass. Check returns its problems, or none. */
interface TieredQualificationProfileRule {
    fun check(item: TieredQualificationProfile): MutableList<String>
}

data class TieredQualificationProfileCodeLengthRule(var maxLength: Int) : TieredQualificationProfileRule {
    override fun check(item: TieredQualificationProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredQualificationProfileRequiredPhoneRule(var message: String) : TieredQualificationProfileRule {
    override fun check(item: TieredQualificationProfile): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredQualificationProfileApprovedGuardRule(var limit: Double) : TieredQualificationProfileRule {
    override fun check(item: TieredQualificationProfile): MutableList<String> {
        if (item.approved && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered qualification profile. */
data class TieredQualificationProfileValidator(var rules: MutableList<TieredQualificationProfileRule>) {
    fun validate(item: TieredQualificationProfile): MutableList<String> {
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

    fun requireValid(item: TieredQualificationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredQualificationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredQualificationProfileValidator(): TieredQualificationProfileValidator {
    return TieredQualificationProfileValidator(
        mutableListOf<TieredQualificationProfileRule>(
            TieredQualificationProfileCodeLengthRule(16),
            TieredQualificationProfileRequiredPhoneRule("tiered qualification profile needs a value"),
            TieredQualificationProfileApprovedGuardRule(6.9),
        ),
    )
}
