package com.acme.transport.network.cutoff.summary

/** A rule a cutoff request must pass. Check returns its problems, or none. */
interface CutoffRequestRule {
    fun check(item: CutoffRequest): MutableList<String>
}

data class CutoffRequestTitleLengthRule(var maxLength: Int) : CutoffRequestRule {
    override fun check(item: CutoffRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffRequestRequiredTitleRule(var message: String) : CutoffRequestRule {
    override fun check(item: CutoffRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffRequestRequiredSegmentRule(var message: String) : CutoffRequestRule {
    override fun check(item: CutoffRequest): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff request. */
data class CutoffRequestValidator(var rules: MutableList<CutoffRequestRule>) {
    fun validate(item: CutoffRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffRequestValidator(): CutoffRequestValidator {
    return CutoffRequestValidator(
        mutableListOf<CutoffRequestRule>(
            CutoffRequestTitleLengthRule(120),
            CutoffRequestRequiredTitleRule("cutoff request needs this field"),
            CutoffRequestRequiredSegmentRule("cutoff request needs an entry"),
        ),
    )
}
