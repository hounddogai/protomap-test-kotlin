package com.acme.transport.linehaul.backhaul.policy

/** A rule a lenient backhaul request must pass. Check returns its problems, or none. */
interface LenientBackhaulRequestRule {
    fun check(item: LenientBackhaulRequest): MutableList<String>
}

data class LenientBackhaulRequestMinimumRangeRule(
    var min: Double,
    var max: Double,
) : LenientBackhaulRequestRule {
    override fun check(item: LenientBackhaulRequest): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class LenientBackhaulRequestNameLengthRule(var maxLength: Int) : LenientBackhaulRequestRule {
    override fun check(item: LenientBackhaulRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulRequestScoreRangeRule(var min: Double, var max: Double) : LenientBackhaulRequestRule {
    override fun check(item: LenientBackhaulRequest): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulRequestRequiredCodeRule(var message: String) : LenientBackhaulRequestRule {
    override fun check(item: LenientBackhaulRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulRequestRequiredDriverIdRule(var message: String) : LenientBackhaulRequestRule {
    override fun check(item: LenientBackhaulRequest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient backhaul request. */
data class LenientBackhaulRequestValidator(var rules: MutableList<LenientBackhaulRequestRule>) {
    fun validate(item: LenientBackhaulRequest): MutableList<String> {
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

    fun requireValid(item: LenientBackhaulRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientBackhaulRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientBackhaulRequestValidator(): LenientBackhaulRequestValidator {
    return LenientBackhaulRequestValidator(
        mutableListOf<LenientBackhaulRequestRule>(
            LenientBackhaulRequestMinimumRangeRule(9.1, 819.4),
            LenientBackhaulRequestNameLengthRule(3600),
            LenientBackhaulRequestScoreRangeRule(5.8, 1136.1),
            LenientBackhaulRequestRequiredCodeRule("lenient backhaul request needs a value"),
            LenientBackhaulRequestRequiredDriverIdRule("lenient backhaul request needs a value"),
        ),
    )
}
