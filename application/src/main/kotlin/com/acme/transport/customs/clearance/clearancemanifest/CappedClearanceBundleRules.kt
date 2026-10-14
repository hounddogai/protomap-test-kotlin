package com.acme.transport.customs.clearance.clearancemanifest

/** A rule a capped clearance bundle must pass. Check returns its problems, or none. */
interface CappedClearanceBundleRule {
    fun check(item: CappedClearanceBundle): MutableList<String>
}

data class CappedClearanceBundleLocaleLengthRule(var maxLength: Int) : CappedClearanceBundleRule {
    override fun check(item: CappedClearanceBundle): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedClearanceBundleCodeLengthRule(var maxLength: Int) : CappedClearanceBundleRule {
    override fun check(item: CappedClearanceBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedClearanceBundleRequiredLocaleRule(var message: String) : CappedClearanceBundleRule {
    override fun check(item: CappedClearanceBundle): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped clearance bundle. */
data class CappedClearanceBundleValidator(var rules: MutableList<CappedClearanceBundleRule>) {
    fun validate(item: CappedClearanceBundle): MutableList<String> {
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

    fun requireValid(item: CappedClearanceBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedClearanceBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedClearanceBundleValidator(): CappedClearanceBundleValidator {
    return CappedClearanceBundleValidator(
        mutableListOf<CappedClearanceBundleRule>(
            CappedClearanceBundleLocaleLengthRule(120),
            CappedClearanceBundleCodeLengthRule(64),
            CappedClearanceBundleRequiredLocaleRule("capped clearance bundle needs this field"),
        ),
    )
}
