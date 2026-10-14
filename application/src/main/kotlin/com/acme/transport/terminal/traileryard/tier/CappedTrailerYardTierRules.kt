package com.acme.transport.terminal.traileryard.tier

/** A rule a capped trailer yard tier must pass. Check returns its problems, or none. */
interface CappedTrailerYardTierRule {
    fun check(item: CappedTrailerYardTier): MutableList<String>
}

data class CappedTrailerYardTierCodeLengthRule(var maxLength: Int) : CappedTrailerYardTierRule {
    override fun check(item: CappedTrailerYardTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerYardTierRequiredCodeRule(var message: String) : CappedTrailerYardTierRule {
    override fun check(item: CappedTrailerYardTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerYardTierRequiredNotesRule(var message: String) : CappedTrailerYardTierRule {
    override fun check(item: CappedTrailerYardTier): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped trailer yard tier. */
data class CappedTrailerYardTierValidator(var rules: MutableList<CappedTrailerYardTierRule>) {
    fun validate(item: CappedTrailerYardTier): MutableList<String> {
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

    fun requireValid(item: CappedTrailerYardTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerYardTier>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerYardTierValidator(): CappedTrailerYardTierValidator {
    return CappedTrailerYardTierValidator(
        mutableListOf<CappedTrailerYardTierRule>(
            CappedTrailerYardTierCodeLengthRule(80),
            CappedTrailerYardTierRequiredCodeRule("capped trailer yard tier needs this field"),
            CappedTrailerYardTierRequiredNotesRule("capped trailer yard tier needs this field"),
        ),
    )
}
