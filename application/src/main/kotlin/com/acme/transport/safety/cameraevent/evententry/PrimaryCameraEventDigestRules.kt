package com.acme.transport.safety.cameraevent.evententry

/** A rule a primary camera event digest must pass. Check returns its problems, or none. */
interface PrimaryCameraEventDigestRule {
    fun check(item: PrimaryCameraEventDigest): MutableList<String>
}

data class PrimaryCameraEventDigestRequiredTitleRule(var message: String) : PrimaryCameraEventDigestRule {
    override fun check(item: PrimaryCameraEventDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventDigestSequenceRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryCameraEventDigestRule {
    override fun check(item: PrimaryCameraEventDigest): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventDigestAmountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryCameraEventDigestRule {
    override fun check(item: PrimaryCameraEventDigest): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary camera event digest. */
data class PrimaryCameraEventDigestValidator(var rules: MutableList<PrimaryCameraEventDigestRule>) {
    fun validate(item: PrimaryCameraEventDigest): MutableList<String> {
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

    fun requireValid(item: PrimaryCameraEventDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCameraEventDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCameraEventDigestValidator(): PrimaryCameraEventDigestValidator {
    return PrimaryCameraEventDigestValidator(
        mutableListOf<PrimaryCameraEventDigestRule>(
            PrimaryCameraEventDigestRequiredTitleRule("primary camera event digest needs an entry"),
            PrimaryCameraEventDigestSequenceRangeRule(16, 64),
            PrimaryCameraEventDigestAmountRangeRule(4.1, 4998.8),
        ),
    )
}
