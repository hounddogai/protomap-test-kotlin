package com.acme.transport.lastmile.parcel.segment

/** A rule a parcel digest must pass. Check returns its problems, or none. */
interface ParcelDigestRule {
    fun check(item: ParcelDigest): MutableList<String>
}

data class ParcelDigestCodeLengthRule(var maxLength: Int) : ParcelDigestRule {
    override fun check(item: ParcelDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelDigestStartsAtRangeRule(var min: Int, var max: Int) : ParcelDigestRule {
    override fun check(item: ParcelDigest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ParcelDigestRequiredCodeRule(var message: String) : ParcelDigestRule {
    override fun check(item: ParcelDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelDigestRequiredTitleRule(var message: String) : ParcelDigestRule {
    override fun check(item: ParcelDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel digest. */
data class ParcelDigestValidator(var rules: MutableList<ParcelDigestRule>) {
    fun validate(item: ParcelDigest): MutableList<String> {
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

    fun requireValid(item: ParcelDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelDigestValidator(): ParcelDigestValidator {
    return ParcelDigestValidator(
        mutableListOf<ParcelDigestRule>(
            ParcelDigestCodeLengthRule(3600),
            ParcelDigestStartsAtRangeRule(16, 80),
            ParcelDigestRequiredCodeRule("parcel digest needs an entry"),
            ParcelDigestRequiredTitleRule("parcel digest needs this field"),
        ),
    )
}
