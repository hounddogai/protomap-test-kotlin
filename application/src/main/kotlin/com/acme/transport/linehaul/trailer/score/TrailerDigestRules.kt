package com.acme.transport.linehaul.trailer.score

/** A rule a trailer digest must pass. Check returns its problems, or none. */
interface TrailerDigestRule {
    fun check(item: TrailerDigest): MutableList<String>
}

data class TrailerDigestCodeLengthRule(var maxLength: Int) : TrailerDigestRule {
    override fun check(item: TrailerDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerDigestRequiredCodeRule(var message: String) : TrailerDigestRule {
    override fun check(item: TrailerDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerDigestShiftCodeLengthRule(var maxLength: Int) : TrailerDigestRule {
    override fun check(item: TrailerDigest): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerDigestTitleLengthRule(var maxLength: Int) : TrailerDigestRule {
    override fun check(item: TrailerDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerDigestRequiredShiftCodeRule(var message: String) : TrailerDigestRule {
    override fun check(item: TrailerDigest): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer digest. */
data class TrailerDigestValidator(var rules: MutableList<TrailerDigestRule>) {
    fun validate(item: TrailerDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerDigestValidator(): TrailerDigestValidator {
    return TrailerDigestValidator(
        mutableListOf<TrailerDigestRule>(
            TrailerDigestCodeLengthRule(255),
            TrailerDigestRequiredCodeRule("trailer digest needs this field"),
            TrailerDigestShiftCodeLengthRule(1000),
            TrailerDigestTitleLengthRule(255),
            TrailerDigestRequiredShiftCodeRule("trailer digest needs this field"),
        ),
    )
}
