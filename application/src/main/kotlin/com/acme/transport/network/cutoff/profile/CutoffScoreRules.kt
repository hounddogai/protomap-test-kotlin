package com.acme.transport.network.cutoff.profile

/** A rule a cutoff score must pass. Check returns its problems, or none. */
interface CutoffScoreRule {
    fun check(item: CutoffScore): MutableList<String>
}

data class CutoffScoreDistanceKmRangeRule(var min: Double, var max: Double) : CutoffScoreRule {
    override fun check(item: CutoffScore): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffScoreRequiredTitleRule(var message: String) : CutoffScoreRule {
    override fun check(item: CutoffScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffScoreTitleLengthRule(var maxLength: Int) : CutoffScoreRule {
    override fun check(item: CutoffScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffScoreCodePrefixRule(var prefix: String) : CutoffScoreRule {
    override fun check(item: CutoffScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffScoreActiveGuardRule(var limit: Double) : CutoffScoreRule {
    override fun check(item: CutoffScore): MutableList<String> {
        if (item.active && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff score. */
data class CutoffScoreValidator(var rules: MutableList<CutoffScoreRule>) {
    fun validate(item: CutoffScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffScore>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffScoreValidator(): CutoffScoreValidator {
    return CutoffScoreValidator(
        mutableListOf<CutoffScoreRule>(
            CutoffScoreDistanceKmRangeRule(1.8, 2284.4),
            CutoffScoreRequiredTitleRule("cutoff score needs this field"),
            CutoffScoreTitleLengthRule(120),
            CutoffScoreCodePrefixRule("Q"),
            CutoffScoreActiveGuardRule(6.6),
        ),
    )
}
