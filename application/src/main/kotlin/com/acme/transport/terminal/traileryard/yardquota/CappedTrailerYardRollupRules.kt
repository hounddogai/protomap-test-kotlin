package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.text.normalize

/** A rule a capped trailer yard rollup must pass. Check returns its problems, or none. */
interface CappedTrailerYardRollupRule {
    fun check(item: CappedTrailerYardRollup): MutableList<String>
}

data class CappedTrailerYardRollupRequiredContactEmailRule(var message: String) : CappedTrailerYardRollupRule {
    override fun check(item: CappedTrailerYardRollup): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerYardRollupContactEmailLengthRule(var maxLength: Int) : CappedTrailerYardRollupRule {
    override fun check(item: CappedTrailerYardRollup): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerYardRollupCountryLengthRule(var maxLength: Int) : CappedTrailerYardRollupRule {
    override fun check(item: CappedTrailerYardRollup): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerYardRollupAllowedLabelRule(
    var allowed: MutableList<String>,
) : CappedTrailerYardRollupRule {
    override fun check(item: CappedTrailerYardRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerYardRollupRequiredLabelRule(var message: String) : CappedTrailerYardRollupRule {
    override fun check(item: CappedTrailerYardRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped trailer yard rollup. */
data class CappedTrailerYardRollupValidator(var rules: MutableList<CappedTrailerYardRollupRule>) {
    fun validate(item: CappedTrailerYardRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTrailerYardRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerYardRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerYardRollupValidator(): CappedTrailerYardRollupValidator {
    return CappedTrailerYardRollupValidator(
        mutableListOf<CappedTrailerYardRollupRule>(
            CappedTrailerYardRollupRequiredContactEmailRule("capped trailer yard rollup needs this field"),
            CappedTrailerYardRollupContactEmailLengthRule(1000),
            CappedTrailerYardRollupCountryLengthRule(86400),
            CappedTrailerYardRollupAllowedLabelRule(mutableListOf<String>("rolling")),
            CappedTrailerYardRollupRequiredLabelRule("capped trailer yard rollup needs a value"),
        ),
    )
}
