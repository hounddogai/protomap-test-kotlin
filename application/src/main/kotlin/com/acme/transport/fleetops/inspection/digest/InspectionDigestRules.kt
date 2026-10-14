package com.acme.transport.fleetops.inspection.digest

/** A rule a inspection digest must pass. Check returns its problems, or none. */
interface InspectionDigestRule {
    fun check(item: InspectionDigest): MutableList<String>
}

data class InspectionDigestUnitLengthRule(var maxLength: Int) : InspectionDigestRule {
    override fun check(item: InspectionDigest): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDigestCodeLengthRule(var maxLength: Int) : InspectionDigestRule {
    override fun check(item: InspectionDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDigestLabelLengthRule(var maxLength: Int) : InspectionDigestRule {
    override fun check(item: InspectionDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDigestRequiredUnitRule(var message: String) : InspectionDigestRule {
    override fun check(item: InspectionDigest): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionDigestRequiredExternalRefRule(var message: String) : InspectionDigestRule {
    override fun check(item: InspectionDigest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionDigestExternalRefLengthRule(var maxLength: Int) : InspectionDigestRule {
    override fun check(item: InspectionDigest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection digest. */
data class InspectionDigestValidator(var rules: MutableList<InspectionDigestRule>) {
    fun validate(item: InspectionDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionDigestValidator(): InspectionDigestValidator {
    return InspectionDigestValidator(
        mutableListOf<InspectionDigestRule>(
            InspectionDigestUnitLengthRule(16),
            InspectionDigestCodeLengthRule(120),
            InspectionDigestLabelLengthRule(86400),
            InspectionDigestRequiredUnitRule("inspection digest needs an entry"),
            InspectionDigestRequiredExternalRefRule("inspection digest needs a value"),
            InspectionDigestExternalRefLengthRule(16),
        ),
    )
}
