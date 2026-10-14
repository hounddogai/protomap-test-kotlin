package com.acme.transport.linehaul.tender.rollup

/** A rule a tender score must pass. Check returns its problems, or none. */
interface TenderScoreRule {
    fun check(item: TenderScore): MutableList<String>
}

data class TenderScoreLabelLengthRule(var maxLength: Int) : TenderScoreRule {
    override fun check(item: TenderScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderScoreTitleLengthRule(var maxLength: Int) : TenderScoreRule {
    override fun check(item: TenderScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderScoreCodeLengthRule(var maxLength: Int) : TenderScoreRule {
    override fun check(item: TenderScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderScoreSummaryLengthRule(var maxLength: Int) : TenderScoreRule {
    override fun check(item: TenderScore): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender score. */
data class TenderScoreValidator(var rules: MutableList<TenderScoreRule>) {
    fun validate(item: TenderScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderScore>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderScoreValidator(): TenderScoreValidator {
    return TenderScoreValidator(
        mutableListOf<TenderScoreRule>(
            TenderScoreLabelLengthRule(255),
            TenderScoreTitleLengthRule(86400),
            TenderScoreCodeLengthRule(64),
            TenderScoreSummaryLengthRule(32),
        ),
    )
}
