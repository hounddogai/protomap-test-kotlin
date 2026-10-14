package com.acme.transport.customs.inspection.batch

/** A rule a inspection bundle must pass. Check returns its problems, or none. */
interface InspectionBundleRule {
    fun check(item: InspectionBundle): MutableList<String>
}

data class InspectionBundleCodePrefixRule(var prefix: String) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBundleTitleLengthRule(var maxLength: Int) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBundleRequiredTitleRule(var message: String) : InspectionBundleRule {
    override fun check(item: InspectionBundle): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection bundle. */
data class InspectionBundleValidator(var rules: MutableList<InspectionBundleRule>) {
    fun validate(item: InspectionBundle): MutableList<String> {
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

    fun requireValid(item: InspectionBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionBundleValidator(): InspectionBundleValidator {
    return InspectionBundleValidator(
        mutableListOf<InspectionBundleRule>(
            InspectionBundleCodePrefixRule("X"),
            InspectionBundleTitleLengthRule(80),
            InspectionBundleRequiredTitleRule("inspection bundle needs a value"),
        ),
    )
}
