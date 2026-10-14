package com.acme.transport.terminal.sortation.window

/** A rule a lenient sortation window must pass. Check returns its problems, or none. */
interface LenientSortationWindowRule {
    fun check(item: LenientSortationWindow): MutableList<String>
}

data class LenientSortationWindowBatchSizeRangeRule(var min: Int, var max: Int) : LenientSortationWindowRule {
    override fun check(item: LenientSortationWindow): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class LenientSortationWindowSequenceRangeRule(var min: Int, var max: Int) : LenientSortationWindowRule {
    override fun check(item: LenientSortationWindow): MutableList<String> {
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

data class LenientSortationWindowCodePrefixRule(var prefix: String) : LenientSortationWindowRule {
    override fun check(item: LenientSortationWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientSortationWindowRequiredNotesRule(var message: String) : LenientSortationWindowRule {
    override fun check(item: LenientSortationWindow): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient sortation window. */
data class LenientSortationWindowValidator(var rules: MutableList<LenientSortationWindowRule>) {
    fun validate(item: LenientSortationWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientSortationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientSortationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientSortationWindowValidator(): LenientSortationWindowValidator {
    return LenientSortationWindowValidator(
        mutableListOf<LenientSortationWindowRule>(
            LenientSortationWindowBatchSizeRangeRule(16, 32),
            LenientSortationWindowSequenceRangeRule(80, 1000),
            LenientSortationWindowCodePrefixRule("REF"),
            LenientSortationWindowRequiredNotesRule("lenient sortation window needs this field"),
        ),
    )
}
