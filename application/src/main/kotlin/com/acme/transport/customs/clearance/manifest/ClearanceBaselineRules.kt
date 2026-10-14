package com.acme.transport.customs.clearance.manifest

/** A rule a clearance baseline must pass. Check returns its problems, or none. */
interface ClearanceBaselineRule {
    fun check(item: ClearanceBaseline): MutableList<String>
}

data class ClearanceBaselineTitleLengthRule(var maxLength: Int) : ClearanceBaselineRule {
    override fun check(item: ClearanceBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBaselineRequiredTitleRule(var message: String) : ClearanceBaselineRule {
    override fun check(item: ClearanceBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBaselineRequiredNameRule(var message: String) : ClearanceBaselineRule {
    override fun check(item: ClearanceBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBaselineRequiredCodeRule(var message: String) : ClearanceBaselineRule {
    override fun check(item: ClearanceBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBaselinePhoneLengthRule(var maxLength: Int) : ClearanceBaselineRule {
    override fun check(item: ClearanceBaseline): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceBaselineNameLengthRule(var maxLength: Int) : ClearanceBaselineRule {
    override fun check(item: ClearanceBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance baseline. */
data class ClearanceBaselineValidator(var rules: MutableList<ClearanceBaselineRule>) {
    fun validate(item: ClearanceBaseline): MutableList<String> {
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

    fun requireValid(item: ClearanceBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceBaselineValidator(): ClearanceBaselineValidator {
    return ClearanceBaselineValidator(
        mutableListOf<ClearanceBaselineRule>(
            ClearanceBaselineTitleLengthRule(80),
            ClearanceBaselineRequiredTitleRule("clearance baseline needs an entry"),
            ClearanceBaselineRequiredNameRule("clearance baseline needs this field"),
            ClearanceBaselineRequiredCodeRule("clearance baseline needs this field"),
            ClearanceBaselinePhoneLengthRule(255),
            ClearanceBaselineNameLengthRule(120),
        ),
    )
}
