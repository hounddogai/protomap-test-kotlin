package com.acme.transport.linehaul.trailer.regionaltrailerrecord

/** A rule a capped trailer record must pass. Check returns its problems, or none. */
interface CappedTrailerRecordRule {
    fun check(item: CappedTrailerRecord): MutableList<String>
}

data class CappedTrailerRecordCodePrefixRule(var prefix: String) : CappedTrailerRecordRule {
    override fun check(item: CappedTrailerRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerRecordApprovedGuardRule(var limit: Double) : CappedTrailerRecordRule {
    override fun check(item: CappedTrailerRecord): MutableList<String> {
        if (item.approved && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerRecordEndsAtRangeRule(var min: Int, var max: Int) : CappedTrailerRecordRule {
    override fun check(item: CappedTrailerRecord): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped trailer record. */
data class CappedTrailerRecordValidator(var rules: MutableList<CappedTrailerRecordRule>) {
    fun validate(item: CappedTrailerRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTrailerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerRecordValidator(): CappedTrailerRecordValidator {
    return CappedTrailerRecordValidator(
        mutableListOf<CappedTrailerRecordRule>(
            CappedTrailerRecordCodePrefixRule("REF"),
            CappedTrailerRecordApprovedGuardRule(1.6),
            CappedTrailerRecordEndsAtRangeRule(80, 3600),
        ),
    )
}
