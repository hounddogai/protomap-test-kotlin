package com.acme.transport.crewing.restbreak.breakprofile

/** A rule a rest break checkpoint must pass. Check returns its problems, or none. */
interface RestBreakCheckpointRule {
    fun check(item: RestBreakCheckpoint): MutableList<String>
}

data class RestBreakCheckpointExpiresAtRangeRule(var min: Int, var max: Int) : RestBreakCheckpointRule {
    override fun check(item: RestBreakCheckpoint): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakCheckpointTitleLengthRule(var maxLength: Int) : RestBreakCheckpointRule {
    override fun check(item: RestBreakCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakCheckpointRequiredCodeRule(var message: String) : RestBreakCheckpointRule {
    override fun check(item: RestBreakCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakCheckpointRequiredTitleRule(var message: String) : RestBreakCheckpointRule {
    override fun check(item: RestBreakCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakCheckpointCodePrefixRule(var prefix: String) : RestBreakCheckpointRule {
    override fun check(item: RestBreakCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break checkpoint. */
data class RestBreakCheckpointValidator(var rules: MutableList<RestBreakCheckpointRule>) {
    fun validate(item: RestBreakCheckpoint): MutableList<String> {
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

    fun requireValid(item: RestBreakCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakCheckpointValidator(): RestBreakCheckpointValidator {
    return RestBreakCheckpointValidator(
        mutableListOf<RestBreakCheckpointRule>(
            RestBreakCheckpointExpiresAtRangeRule(120, 3600),
            RestBreakCheckpointTitleLengthRule(32),
            RestBreakCheckpointRequiredCodeRule("rest break checkpoint needs this field"),
            RestBreakCheckpointRequiredTitleRule("rest break checkpoint needs an entry"),
            RestBreakCheckpointCodePrefixRule("REF"),
        ),
    )
}
