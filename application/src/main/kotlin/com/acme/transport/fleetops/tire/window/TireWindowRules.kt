package com.acme.transport.fleetops.tire.window

/** A rule a tire window must pass. Check returns its problems, or none. */
interface TireWindowRule {
    fun check(item: TireWindow): MutableList<String>
}

data class TireWindowSummaryLengthRule(var maxLength: Int) : TireWindowRule {
    override fun check(item: TireWindow): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireWindowArchivedGuardRule(var limit: Int) : TireWindowRule {
    override fun check(item: TireWindow): MutableList<String> {
        if (item.archived && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class TireWindowTitleLengthRule(var maxLength: Int) : TireWindowRule {
    override fun check(item: TireWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireWindowCodeLengthRule(var maxLength: Int) : TireWindowRule {
    override fun check(item: TireWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire window. */
data class TireWindowValidator(var rules: MutableList<TireWindowRule>) {
    fun validate(item: TireWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultTireWindowValidator(): TireWindowValidator {
    return TireWindowValidator(
        mutableListOf<TireWindowRule>(
            TireWindowSummaryLengthRule(1000),
            TireWindowArchivedGuardRule(86400),
            TireWindowTitleLengthRule(80),
            TireWindowCodeLengthRule(3600),
        ),
    )
}
