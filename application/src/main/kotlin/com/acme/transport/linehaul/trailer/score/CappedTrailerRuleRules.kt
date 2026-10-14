package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.text.normalize

/** A rule a capped trailer rule must pass. Check returns its problems, or none. */
interface CappedTrailerRuleRule {
    fun check(item: CappedTrailerRule): MutableList<String>
}

data class CappedTrailerRuleCodeLengthRule(var maxLength: Int) : CappedTrailerRuleRule {
    override fun check(item: CappedTrailerRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerRuleAllowedDockRule(var allowed: MutableList<String>) : CappedTrailerRuleRule {
    override fun check(item: CappedTrailerRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerRuleSourceLengthRule(var maxLength: Int) : CappedTrailerRuleRule {
    override fun check(item: CappedTrailerRule): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerRuleCreatedAtRangeRule(var min: Int, var max: Int) : CappedTrailerRuleRule {
    override fun check(item: CappedTrailerRule): MutableList<String> {
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

data class CappedTrailerRuleSequenceRangeRule(var min: Int, var max: Int) : CappedTrailerRuleRule {
    override fun check(item: CappedTrailerRule): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class CappedTrailerRuleRequiredCodeRule(var message: String) : CappedTrailerRuleRule {
    override fun check(item: CappedTrailerRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped trailer rule. */
data class CappedTrailerRuleValidator(var rules: MutableList<CappedTrailerRuleRule>) {
    fun validate(item: CappedTrailerRule): MutableList<String> {
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

    fun requireValid(item: CappedTrailerRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerRule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerRuleValidator(): CappedTrailerRuleValidator {
    return CappedTrailerRuleValidator(
        mutableListOf<CappedTrailerRuleRule>(
            CappedTrailerRuleCodeLengthRule(120),
            CappedTrailerRuleAllowedDockRule(mutableListOf<String>()),
            CappedTrailerRuleSourceLengthRule(32),
            CappedTrailerRuleCreatedAtRangeRule(255, 3600),
            CappedTrailerRuleSequenceRangeRule(64, 255),
            CappedTrailerRuleRequiredCodeRule("capped trailer rule needs an entry"),
        ),
    )
}
