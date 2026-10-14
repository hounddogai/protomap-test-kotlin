package com.acme.transport.terminal.dwell.digest

/** A rule a fallback dwell score must pass. Check returns its problems, or none. */
interface FallbackDwellScoreRule {
    fun check(item: FallbackDwellScore): MutableList<String>
}

data class FallbackDwellScoreRequiredShiftCodeRule(var message: String) : FallbackDwellScoreRule {
    override fun check(item: FallbackDwellScore): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellScoreCodeLengthRule(var maxLength: Int) : FallbackDwellScoreRule {
    override fun check(item: FallbackDwellScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellScoreNameLengthRule(var maxLength: Int) : FallbackDwellScoreRule {
    override fun check(item: FallbackDwellScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dwell score. */
data class FallbackDwellScoreValidator(var rules: MutableList<FallbackDwellScoreRule>) {
    fun validate(item: FallbackDwellScore): MutableList<String> {
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

    fun requireValid(item: FallbackDwellScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDwellScore>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDwellScoreValidator(): FallbackDwellScoreValidator {
    return FallbackDwellScoreValidator(
        mutableListOf<FallbackDwellScoreRule>(
            FallbackDwellScoreRequiredShiftCodeRule("fallback dwell score needs an entry"),
            FallbackDwellScoreCodeLengthRule(64),
            FallbackDwellScoreNameLengthRule(120),
        ),
    )
}
