package com.acme.transport.lastmile.drop.entry

/** A rule a drop entry must pass. Check returns its problems, or none. */
interface DropEntryRule {
    fun check(item: DropEntry): MutableList<String>
}

data class DropEntryRegionLengthRule(var maxLength: Int) : DropEntryRule {
    override fun check(item: DropEntry): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropEntryRequiredReviewerRule(var message: String) : DropEntryRule {
    override fun check(item: DropEntry): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropEntryRequiredCodeRule(var message: String) : DropEntryRule {
    override fun check(item: DropEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropEntryCodeLengthRule(var maxLength: Int) : DropEntryRule {
    override fun check(item: DropEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop entry. */
data class DropEntryValidator(var rules: MutableList<DropEntryRule>) {
    fun validate(item: DropEntry): MutableList<String> {
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

    fun requireValid(item: DropEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDropEntryValidator(): DropEntryValidator {
    return DropEntryValidator(
        mutableListOf<DropEntryRule>(
            DropEntryRegionLengthRule(80),
            DropEntryRequiredReviewerRule("drop entry needs an entry"),
            DropEntryRequiredCodeRule("drop entry needs an entry"),
            DropEntryCodeLengthRule(86400),
        ),
    )
}
