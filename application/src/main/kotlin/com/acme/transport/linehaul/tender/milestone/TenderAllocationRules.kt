package com.acme.transport.linehaul.tender.milestone

/** A rule a tender allocation must pass. Check returns its problems, or none. */
interface TenderAllocationRule {
    fun check(item: TenderAllocation): MutableList<String>
}

data class TenderAllocationRequiredRegionRule(var message: String) : TenderAllocationRule {
    override fun check(item: TenderAllocation): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderAllocationCategoryLengthRule(var maxLength: Int) : TenderAllocationRule {
    override fun check(item: TenderAllocation): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderAllocationLabelLengthRule(var maxLength: Int) : TenderAllocationRule {
    override fun check(item: TenderAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderAllocationRegionLengthRule(var maxLength: Int) : TenderAllocationRule {
    override fun check(item: TenderAllocation): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender allocation. */
data class TenderAllocationValidator(var rules: MutableList<TenderAllocationRule>) {
    fun validate(item: TenderAllocation): MutableList<String> {
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

    fun requireValid(item: TenderAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderAllocationValidator(): TenderAllocationValidator {
    return TenderAllocationValidator(
        mutableListOf<TenderAllocationRule>(
            TenderAllocationRequiredRegionRule("tender allocation needs this field"),
            TenderAllocationCategoryLengthRule(3600),
            TenderAllocationLabelLengthRule(80),
            TenderAllocationRegionLengthRule(255),
        ),
    )
}
