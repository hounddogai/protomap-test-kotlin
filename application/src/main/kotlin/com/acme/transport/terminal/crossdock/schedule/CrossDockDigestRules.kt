package com.acme.transport.terminal.crossdock.schedule

/** A rule a cross dock digest must pass. Check returns its problems, or none. */
interface CrossDockDigestRule {
    fun check(item: CrossDockDigest): MutableList<String>
}

data class CrossDockDigestDockLengthRule(var maxLength: Int) : CrossDockDigestRule {
    override fun check(item: CrossDockDigest): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockDigestRateRangeRule(var min: Double, var max: Double) : CrossDockDigestRule {
    override fun check(item: CrossDockDigest): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockDigestRequiredTitleRule(var message: String) : CrossDockDigestRule {
    override fun check(item: CrossDockDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockDigestRevisionRangeRule(var min: Int, var max: Int) : CrossDockDigestRule {
    override fun check(item: CrossDockDigest): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock digest. */
data class CrossDockDigestValidator(var rules: MutableList<CrossDockDigestRule>) {
    fun validate(item: CrossDockDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockDigestValidator(): CrossDockDigestValidator {
    return CrossDockDigestValidator(
        mutableListOf<CrossDockDigestRule>(
            CrossDockDigestDockLengthRule(255),
            CrossDockDigestRateRangeRule(4.7, 1938.6),
            CrossDockDigestRequiredTitleRule("cross dock digest needs an entry"),
            CrossDockDigestRevisionRangeRule(120, 1000),
        ),
    )
}
