package com.acme.transport.terminal.crossdock.assignment

/** A rule a cross dock entry must pass. Check returns its problems, or none. */
interface CrossDockEntryRule {
    fun check(item: CrossDockEntry): MutableList<String>
}

data class CrossDockEntryPalletsRangeRule(var min: Int, var max: Int) : CrossDockEntryRule {
    override fun check(item: CrossDockEntry): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class CrossDockEntrySummaryLengthRule(var maxLength: Int) : CrossDockEntryRule {
    override fun check(item: CrossDockEntry): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockEntryRequiredContactEmailRule(var message: String) : CrossDockEntryRule {
    override fun check(item: CrossDockEntry): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockEntryRequiredTitleRule(var message: String) : CrossDockEntryRule {
    override fun check(item: CrossDockEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockEntryCodeLengthRule(var maxLength: Int) : CrossDockEntryRule {
    override fun check(item: CrossDockEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock entry. */
data class CrossDockEntryValidator(var rules: MutableList<CrossDockEntryRule>) {
    fun validate(item: CrossDockEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockEntryValidator(): CrossDockEntryValidator {
    return CrossDockEntryValidator(
        mutableListOf<CrossDockEntryRule>(
            CrossDockEntryPalletsRangeRule(120, 255),
            CrossDockEntrySummaryLengthRule(80),
            CrossDockEntryRequiredContactEmailRule("cross dock entry needs an entry"),
            CrossDockEntryRequiredTitleRule("cross dock entry needs a value"),
            CrossDockEntryCodeLengthRule(3600),
        ),
    )
}
