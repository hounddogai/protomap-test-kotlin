package com.acme.transport.lastmile.drop.cappeddropmilestone

/** A rule a primary drop entry must pass. Check returns its problems, or none. */
interface PrimaryDropEntryRule {
    fun check(item: PrimaryDropEntry): MutableList<String>
}

data class PrimaryDropEntryEtaMinutesRangeRule(var min: Int, var max: Int) : PrimaryDropEntryRule {
    override fun check(item: PrimaryDropEntry): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropEntryTitleLengthRule(var maxLength: Int) : PrimaryDropEntryRule {
    override fun check(item: PrimaryDropEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropEntryApprovedGuardRule(var limit: Int) : PrimaryDropEntryRule {
    override fun check(item: PrimaryDropEntry): MutableList<String> {
        if (item.approved && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropEntryContactEmailLengthRule(var maxLength: Int) : PrimaryDropEntryRule {
    override fun check(item: PrimaryDropEntry): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropEntryCodeLengthRule(var maxLength: Int) : PrimaryDropEntryRule {
    override fun check(item: PrimaryDropEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDropEntryRequiredCodeRule(var message: String) : PrimaryDropEntryRule {
    override fun check(item: PrimaryDropEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary drop entry. */
data class PrimaryDropEntryValidator(var rules: MutableList<PrimaryDropEntryRule>) {
    fun validate(item: PrimaryDropEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDropEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDropEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDropEntryValidator(): PrimaryDropEntryValidator {
    return PrimaryDropEntryValidator(
        mutableListOf<PrimaryDropEntryRule>(
            PrimaryDropEntryEtaMinutesRangeRule(3600, 86400),
            PrimaryDropEntryTitleLengthRule(120),
            PrimaryDropEntryApprovedGuardRule(86400),
            PrimaryDropEntryContactEmailLengthRule(64),
            PrimaryDropEntryCodeLengthRule(32),
            PrimaryDropEntryRequiredCodeRule("primary drop entry needs a value"),
        ),
    )
}
