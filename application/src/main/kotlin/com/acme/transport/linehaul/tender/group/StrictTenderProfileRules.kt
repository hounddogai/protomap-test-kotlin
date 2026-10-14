package com.acme.transport.linehaul.tender.group

/** A rule a strict tender profile must pass. Check returns its problems, or none. */
interface StrictTenderProfileRule {
    fun check(item: StrictTenderProfile): MutableList<String>
}

data class StrictTenderProfileRequiredLabelRule(var message: String) : StrictTenderProfileRule {
    override fun check(item: StrictTenderProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderProfileCodeLengthRule(var maxLength: Int) : StrictTenderProfileRule {
    override fun check(item: StrictTenderProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderProfileCapacityRangeRule(var min: Int, var max: Int) : StrictTenderProfileRule {
    override fun check(item: StrictTenderProfile): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderProfileRequiredCodeRule(var message: String) : StrictTenderProfileRule {
    override fun check(item: StrictTenderProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTenderProfileBatchSizeRangeRule(var min: Int, var max: Int) : StrictTenderProfileRule {
    override fun check(item: StrictTenderProfile): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict tender profile. */
data class StrictTenderProfileValidator(var rules: MutableList<StrictTenderProfileRule>) {
    fun validate(item: StrictTenderProfile): MutableList<String> {
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

    fun requireValid(item: StrictTenderProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTenderProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTenderProfileValidator(): StrictTenderProfileValidator {
    return StrictTenderProfileValidator(
        mutableListOf<StrictTenderProfileRule>(
            StrictTenderProfileRequiredLabelRule("strict tender profile needs an entry"),
            StrictTenderProfileCodeLengthRule(32),
            StrictTenderProfileCapacityRangeRule(32, 64),
            StrictTenderProfileRequiredCodeRule("strict tender profile needs a value"),
            StrictTenderProfileBatchSizeRangeRule(16, 80),
        ),
    )
}
