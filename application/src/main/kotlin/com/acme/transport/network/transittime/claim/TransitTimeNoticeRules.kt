package com.acme.transport.network.transittime.claim

/** A rule a transit time notice must pass. Check returns its problems, or none. */
interface TransitTimeNoticeRule {
    fun check(item: TransitTimeNotice): MutableList<String>
}

data class TransitTimeNoticeCreatedAtRangeRule(var min: Int, var max: Int) : TransitTimeNoticeRule {
    override fun check(item: TransitTimeNotice): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class TransitTimeNoticeRequiredDriverIdRule(var message: String) : TransitTimeNoticeRule {
    override fun check(item: TransitTimeNotice): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeNoticeRequiredCodeRule(var message: String) : TransitTimeNoticeRule {
    override fun check(item: TransitTimeNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeNoticeDriverIdLengthRule(var maxLength: Int) : TransitTimeNoticeRule {
    override fun check(item: TransitTimeNotice): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time notice. */
data class TransitTimeNoticeValidator(var rules: MutableList<TransitTimeNoticeRule>) {
    fun validate(item: TransitTimeNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeNoticeValidator(): TransitTimeNoticeValidator {
    return TransitTimeNoticeValidator(
        mutableListOf<TransitTimeNoticeRule>(
            TransitTimeNoticeCreatedAtRangeRule(16, 64),
            TransitTimeNoticeRequiredDriverIdRule("transit time notice needs a value"),
            TransitTimeNoticeRequiredCodeRule("transit time notice needs this field"),
            TransitTimeNoticeDriverIdLengthRule(80),
        ),
    )
}
