package com.acme.transport.linehaul.capacity.rollup

/** A rule a capacity entry must pass. Check returns its problems, or none. */
interface CapacityEntryRule {
    fun check(item: CapacityEntry): MutableList<String>
}

data class CapacityEntryRequiredCodeRule(var message: String) : CapacityEntryRule {
    override fun check(item: CapacityEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityEntryRequiredCategoryRule(var message: String) : CapacityEntryRule {
    override fun check(item: CapacityEntry): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityEntryTitleLengthRule(var maxLength: Int) : CapacityEntryRule {
    override fun check(item: CapacityEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity entry. */
data class CapacityEntryValidator(var rules: MutableList<CapacityEntryRule>) {
    fun validate(item: CapacityEntry): MutableList<String> {
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

    fun requireValid(item: CapacityEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityEntryValidator(): CapacityEntryValidator {
    return CapacityEntryValidator(
        mutableListOf<CapacityEntryRule>(
            CapacityEntryRequiredCodeRule("capacity entry needs an entry"),
            CapacityEntryRequiredCategoryRule("capacity entry needs a value"),
            CapacityEntryTitleLengthRule(1000),
        ),
    )
}
