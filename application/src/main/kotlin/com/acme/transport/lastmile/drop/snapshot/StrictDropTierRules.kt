package com.acme.transport.lastmile.drop.snapshot

/** A rule a strict drop tier must pass. Check returns its problems, or none. */
interface StrictDropTierRule {
    fun check(item: StrictDropTier): MutableList<String>
}

data class StrictDropTierRequiredDriverIdRule(var message: String) : StrictDropTierRule {
    override fun check(item: StrictDropTier): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDropTierCodeLengthRule(var maxLength: Int) : StrictDropTierRule {
    override fun check(item: StrictDropTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDropTierDriverIdLengthRule(var maxLength: Int) : StrictDropTierRule {
    override fun check(item: StrictDropTier): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict drop tier. */
data class StrictDropTierValidator(var rules: MutableList<StrictDropTierRule>) {
    fun validate(item: StrictDropTier): MutableList<String> {
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

    fun requireValid(item: StrictDropTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDropTier>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDropTierValidator(): StrictDropTierValidator {
    return StrictDropTierValidator(
        mutableListOf<StrictDropTierRule>(
            StrictDropTierRequiredDriverIdRule("strict drop tier needs this field"),
            StrictDropTierCodeLengthRule(3600),
            StrictDropTierDriverIdLengthRule(3600),
        ),
    )
}
