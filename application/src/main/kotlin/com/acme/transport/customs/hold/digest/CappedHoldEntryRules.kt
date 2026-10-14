package com.acme.transport.customs.hold.digest

/** A rule a capped hold entry must pass. Check returns its problems, or none. */
interface CappedHoldEntryRule {
    fun check(item: CappedHoldEntry): MutableList<String>
}

data class CappedHoldEntryPalletsRangeRule(var min: Int, var max: Int) : CappedHoldEntryRule {
    override fun check(item: CappedHoldEntry): MutableList<String> {
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

data class CappedHoldEntryRequiredTitleRule(var message: String) : CappedHoldEntryRule {
    override fun check(item: CappedHoldEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldEntryRequiredNotesRule(var message: String) : CappedHoldEntryRule {
    override fun check(item: CappedHoldEntry): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldEntryTitleLengthRule(var maxLength: Int) : CappedHoldEntryRule {
    override fun check(item: CappedHoldEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldEntryCodePrefixRule(var prefix: String) : CappedHoldEntryRule {
    override fun check(item: CappedHoldEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldEntryNotesLengthRule(var maxLength: Int) : CappedHoldEntryRule {
    override fun check(item: CappedHoldEntry): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped hold entry. */
data class CappedHoldEntryValidator(var rules: MutableList<CappedHoldEntryRule>) {
    fun validate(item: CappedHoldEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedHoldEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedHoldEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedHoldEntryValidator(): CappedHoldEntryValidator {
    return CappedHoldEntryValidator(
        mutableListOf<CappedHoldEntryRule>(
            CappedHoldEntryPalletsRangeRule(64, 255),
            CappedHoldEntryRequiredTitleRule("capped hold entry needs a value"),
            CappedHoldEntryRequiredNotesRule("capped hold entry needs a value"),
            CappedHoldEntryTitleLengthRule(32),
            CappedHoldEntryCodePrefixRule("X"),
            CappedHoldEntryNotesLengthRule(3600),
        ),
    )
}
