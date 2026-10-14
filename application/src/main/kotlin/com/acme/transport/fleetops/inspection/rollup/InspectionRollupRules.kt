package com.acme.transport.fleetops.inspection.rollup

/** A rule a inspection rollup must pass. Check returns its problems, or none. */
interface InspectionRollupRule {
    fun check(item: InspectionRollup): MutableList<String>
}

data class InspectionRollupCodeLengthRule(var maxLength: Int) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRollupPinnedGuardRule(var limit: Double) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRollupRequiredContactEmailRule(var message: String) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRollupRequiredLabelRule(var message: String) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRollupContactEmailLengthRule(var maxLength: Int) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRollupRequiredCountryRule(var message: String) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection rollup. */
data class InspectionRollupValidator(var rules: MutableList<InspectionRollupRule>) {
    fun validate(item: InspectionRollup): MutableList<String> {
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

    fun requireValid(item: InspectionRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionRollupValidator(): InspectionRollupValidator {
    return InspectionRollupValidator(
        mutableListOf<InspectionRollupRule>(
            InspectionRollupCodeLengthRule(64),
            InspectionRollupPinnedGuardRule(3.2),
            InspectionRollupRequiredContactEmailRule("inspection rollup needs a value"),
            InspectionRollupRequiredLabelRule("inspection rollup needs a value"),
            InspectionRollupContactEmailLengthRule(120),
            InspectionRollupRequiredCountryRule("inspection rollup needs a value"),
        ),
    )
}
