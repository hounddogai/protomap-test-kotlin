package com.acme.transport.customs.clearance.variance

/** A rule a clearance variance must pass. Check returns its problems, or none. */
interface ClearanceVarianceRule {
    fun check(item: ClearanceVariance): MutableList<String>
}

data class ClearanceVarianceLabelLengthRule(var maxLength: Int) : ClearanceVarianceRule {
    override fun check(item: ClearanceVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceVarianceCodeLengthRule(var maxLength: Int) : ClearanceVarianceRule {
    override fun check(item: ClearanceVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceVarianceRequiredLocaleRule(var message: String) : ClearanceVarianceRule {
    override fun check(item: ClearanceVariance): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceVarianceLocaleLengthRule(var maxLength: Int) : ClearanceVarianceRule {
    override fun check(item: ClearanceVariance): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance variance. */
data class ClearanceVarianceValidator(var rules: MutableList<ClearanceVarianceRule>) {
    fun validate(item: ClearanceVariance): MutableList<String> {
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

    fun requireValid(item: ClearanceVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceVarianceValidator(): ClearanceVarianceValidator {
    return ClearanceVarianceValidator(
        mutableListOf<ClearanceVarianceRule>(
            ClearanceVarianceLabelLengthRule(3600),
            ClearanceVarianceCodeLengthRule(32),
            ClearanceVarianceRequiredLocaleRule("clearance variance needs a value"),
            ClearanceVarianceLocaleLengthRule(32),
        ),
    )
}
