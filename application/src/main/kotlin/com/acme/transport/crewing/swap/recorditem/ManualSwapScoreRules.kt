package com.acme.transport.crewing.swap.recorditem

/** A rule a manual swap score must pass. Check returns its problems, or none. */
interface ManualSwapScoreRule {
    fun check(item: ManualSwapScore): MutableList<String>
}

data class ManualSwapScoreRequiredCodeRule(var message: String) : ManualSwapScoreRule {
    override fun check(item: ManualSwapScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualSwapScoreTitleLengthRule(var maxLength: Int) : ManualSwapScoreRule {
    override fun check(item: ManualSwapScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualSwapScoreRequiredTitleRule(var message: String) : ManualSwapScoreRule {
    override fun check(item: ManualSwapScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualSwapScoreChecksumLengthRule(var maxLength: Int) : ManualSwapScoreRule {
    override fun check(item: ManualSwapScore): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual swap score. */
data class ManualSwapScoreValidator(var rules: MutableList<ManualSwapScoreRule>) {
    fun validate(item: ManualSwapScore): MutableList<String> {
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

    fun requireValid(item: ManualSwapScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualSwapScore>): MutableMap<String, MutableList<String>> {
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

fun defaultManualSwapScoreValidator(): ManualSwapScoreValidator {
    return ManualSwapScoreValidator(
        mutableListOf<ManualSwapScoreRule>(
            ManualSwapScoreRequiredCodeRule("manual swap score needs this field"),
            ManualSwapScoreTitleLengthRule(32),
            ManualSwapScoreRequiredTitleRule("manual swap score needs a value"),
            ManualSwapScoreChecksumLengthRule(255),
        ),
    )
}
