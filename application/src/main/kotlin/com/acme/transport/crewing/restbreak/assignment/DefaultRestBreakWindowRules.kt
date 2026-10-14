package com.acme.transport.crewing.restbreak.assignment

/** A rule a default rest break window must pass. Check returns its problems, or none. */
interface DefaultRestBreakWindowRule {
    fun check(item: DefaultRestBreakWindow): MutableList<String>
}

data class DefaultRestBreakWindowCodePrefixRule(var prefix: String) : DefaultRestBreakWindowRule {
    override fun check(item: DefaultRestBreakWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakWindowRequiredCodeRule(var message: String) : DefaultRestBreakWindowRule {
    override fun check(item: DefaultRestBreakWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakWindowRequiredTitleRule(var message: String) : DefaultRestBreakWindowRule {
    override fun check(item: DefaultRestBreakWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRestBreakWindowBatchSizeRangeRule(var min: Int, var max: Int) : DefaultRestBreakWindowRule {
    override fun check(item: DefaultRestBreakWindow): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default rest break window. */
data class DefaultRestBreakWindowValidator(var rules: MutableList<DefaultRestBreakWindowRule>) {
    fun validate(item: DefaultRestBreakWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRestBreakWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRestBreakWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRestBreakWindowValidator(): DefaultRestBreakWindowValidator {
    return DefaultRestBreakWindowValidator(
        mutableListOf<DefaultRestBreakWindowRule>(
            DefaultRestBreakWindowCodePrefixRule("REF"),
            DefaultRestBreakWindowRequiredCodeRule("default rest break window needs a value"),
            DefaultRestBreakWindowRequiredTitleRule("default rest break window needs an entry"),
            DefaultRestBreakWindowBatchSizeRangeRule(64, 1000),
        ),
    )
}
