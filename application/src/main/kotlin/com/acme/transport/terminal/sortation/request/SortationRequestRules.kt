package com.acme.transport.terminal.sortation.request

/** A rule a sortation request must pass. Check returns its problems, or none. */
interface SortationRequestRule {
    fun check(item: SortationRequest): MutableList<String>
}

data class SortationRequestApprovedGuardRule(var limit: Int) : SortationRequestRule {
    override fun check(item: SortationRequest): MutableList<String> {
        if (item.approved && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class SortationRequestCodePrefixRule(var prefix: String) : SortationRequestRule {
    override fun check(item: SortationRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationRequestRevisionRangeRule(var min: Int, var max: Int) : SortationRequestRule {
    override fun check(item: SortationRequest): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class SortationRequestRequiredLabelRule(var message: String) : SortationRequestRule {
    override fun check(item: SortationRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation request. */
data class SortationRequestValidator(var rules: MutableList<SortationRequestRule>) {
    fun validate(item: SortationRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationRequestValidator(): SortationRequestValidator {
    return SortationRequestValidator(
        mutableListOf<SortationRequestRule>(
            SortationRequestApprovedGuardRule(32),
            SortationRequestCodePrefixRule("REF"),
            SortationRequestRevisionRangeRule(120, 120),
            SortationRequestRequiredLabelRule("sortation request needs this field"),
        ),
    )
}
