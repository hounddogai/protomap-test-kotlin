package com.acme.transport.lastmile.drop.exception

/** A rule a drop exception must pass. Check returns its problems, or none. */
interface DropExceptionRule {
    fun check(item: DropException): MutableList<String>
}

data class DropExceptionNameLengthRule(var maxLength: Int) : DropExceptionRule {
    override fun check(item: DropException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropExceptionRequiredChannelRule(var message: String) : DropExceptionRule {
    override fun check(item: DropException): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropExceptionRequiredCodeRule(var message: String) : DropExceptionRule {
    override fun check(item: DropException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop exception. */
data class DropExceptionValidator(var rules: MutableList<DropExceptionRule>) {
    fun validate(item: DropException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropException>): MutableMap<String, MutableList<String>> {
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

fun defaultDropExceptionValidator(): DropExceptionValidator {
    return DropExceptionValidator(
        mutableListOf<DropExceptionRule>(
            DropExceptionNameLengthRule(1000),
            DropExceptionRequiredChannelRule("drop exception needs an entry"),
            DropExceptionRequiredCodeRule("drop exception needs an entry"),
        ),
    )
}
