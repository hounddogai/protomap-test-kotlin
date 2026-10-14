package com.acme.transport.network.cutoff.batch

/** A rule a primary cutoff score must pass. Check returns its problems, or none. */
interface PrimaryCutoffScoreRule {
    fun check(item: PrimaryCutoffScore): MutableList<String>
}

data class PrimaryCutoffScoreRequiredContactEmailRule(var message: String) : PrimaryCutoffScoreRule {
    override fun check(item: PrimaryCutoffScore): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffScoreRequiredLabelRule(var message: String) : PrimaryCutoffScoreRule {
    override fun check(item: PrimaryCutoffScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffScoreRequiredUnitRule(var message: String) : PrimaryCutoffScoreRule {
    override fun check(item: PrimaryCutoffScore): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary cutoff score. */
data class PrimaryCutoffScoreValidator(var rules: MutableList<PrimaryCutoffScoreRule>) {
    fun validate(item: PrimaryCutoffScore): MutableList<String> {
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

    fun requireValid(item: PrimaryCutoffScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCutoffScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCutoffScoreValidator(): PrimaryCutoffScoreValidator {
    return PrimaryCutoffScoreValidator(
        mutableListOf<PrimaryCutoffScoreRule>(
            PrimaryCutoffScoreRequiredContactEmailRule("primary cutoff score needs an entry"),
            PrimaryCutoffScoreRequiredLabelRule("primary cutoff score needs this field"),
            PrimaryCutoffScoreRequiredUnitRule("primary cutoff score needs this field"),
        ),
    )
}
