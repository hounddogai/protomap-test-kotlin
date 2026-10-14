package com.acme.transport.linehaul.capacity.segment

/** A rule a default capacity exception must pass. Check returns its problems, or none. */
interface DefaultCapacityExceptionRule {
    fun check(item: DefaultCapacityException): MutableList<String>
}

data class DefaultCapacityExceptionSourceLengthRule(var maxLength: Int) : DefaultCapacityExceptionRule {
    override fun check(item: DefaultCapacityException): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityExceptionRequiredCodeRule(var message: String) : DefaultCapacityExceptionRule {
    override fun check(item: DefaultCapacityException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityExceptionRequiredSourceRule(var message: String) : DefaultCapacityExceptionRule {
    override fun check(item: DefaultCapacityException): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityExceptionLabelLengthRule(var maxLength: Int) : DefaultCapacityExceptionRule {
    override fun check(item: DefaultCapacityException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default capacity exception. */
data class DefaultCapacityExceptionValidator(var rules: MutableList<DefaultCapacityExceptionRule>) {
    fun validate(item: DefaultCapacityException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCapacityException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCapacityException>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCapacityExceptionValidator(): DefaultCapacityExceptionValidator {
    return DefaultCapacityExceptionValidator(
        mutableListOf<DefaultCapacityExceptionRule>(
            DefaultCapacityExceptionSourceLengthRule(1000),
            DefaultCapacityExceptionRequiredCodeRule("default capacity exception needs this field"),
            DefaultCapacityExceptionRequiredSourceRule("default capacity exception needs an entry"),
            DefaultCapacityExceptionLabelLengthRule(255),
        ),
    )
}
