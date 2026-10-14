package com.acme.transport.terminal.dwell.plan

/** A rule a dwell window must pass. Check returns its problems, or none. */
interface DwellWindowRule {
    fun check(item: DwellWindow): MutableList<String>
}

data class DwellWindowRequiredCodeRule(var message: String) : DwellWindowRule {
    override fun check(item: DwellWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellWindowRequiredLabelRule(var message: String) : DwellWindowRule {
    override fun check(item: DwellWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellWindowCodeLengthRule(var maxLength: Int) : DwellWindowRule {
    override fun check(item: DwellWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellWindowUnitLengthRule(var maxLength: Int) : DwellWindowRule {
    override fun check(item: DwellWindow): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellWindowCodePrefixRule(var prefix: String) : DwellWindowRule {
    override fun check(item: DwellWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell window. */
data class DwellWindowValidator(var rules: MutableList<DwellWindowRule>) {
    fun validate(item: DwellWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellWindowValidator(): DwellWindowValidator {
    return DwellWindowValidator(
        mutableListOf<DwellWindowRule>(
            DwellWindowRequiredCodeRule("dwell window needs a value"),
            DwellWindowRequiredLabelRule("dwell window needs an entry"),
            DwellWindowCodeLengthRule(120),
            DwellWindowUnitLengthRule(32),
            DwellWindowCodePrefixRule("Q"),
        ),
    )
}
