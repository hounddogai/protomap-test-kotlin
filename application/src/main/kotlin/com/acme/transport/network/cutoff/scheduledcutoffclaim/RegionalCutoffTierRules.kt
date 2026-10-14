package com.acme.transport.network.cutoff.scheduledcutoffclaim

/** A rule a regional cutoff tier must pass. Check returns its problems, or none. */
interface RegionalCutoffTierRule {
    fun check(item: RegionalCutoffTier): MutableList<String>
}

data class RegionalCutoffTierRequiredTitleRule(var message: String) : RegionalCutoffTierRule {
    override fun check(item: RegionalCutoffTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffTierCodeLengthRule(var maxLength: Int) : RegionalCutoffTierRule {
    override fun check(item: RegionalCutoffTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCutoffTierTitleLengthRule(var maxLength: Int) : RegionalCutoffTierRule {
    override fun check(item: RegionalCutoffTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional cutoff tier. */
data class RegionalCutoffTierValidator(var rules: MutableList<RegionalCutoffTierRule>) {
    fun validate(item: RegionalCutoffTier): MutableList<String> {
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

    fun requireValid(item: RegionalCutoffTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCutoffTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCutoffTierValidator(): RegionalCutoffTierValidator {
    return RegionalCutoffTierValidator(
        mutableListOf<RegionalCutoffTierRule>(
            RegionalCutoffTierRequiredTitleRule("regional cutoff tier needs this field"),
            RegionalCutoffTierCodeLengthRule(80),
            RegionalCutoffTierTitleLengthRule(3600),
        ),
    )
}
