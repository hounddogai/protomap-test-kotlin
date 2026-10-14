package com.acme.transport.customs.inspection.adjustment

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

data class InspectionRollupRequiredRegionRule(var message: String) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRollupLabelLengthRule(var maxLength: Int) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRollupRegionLengthRule(var maxLength: Int) : InspectionRollupRule {
    override fun check(item: InspectionRollup): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection rollup. */
data class InspectionRollupValidator(var rules: MutableList<InspectionRollupRule>) {
    fun validate(item: InspectionRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
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
            InspectionRollupCodeLengthRule(86400),
            InspectionRollupRequiredRegionRule("inspection rollup needs this field"),
            InspectionRollupLabelLengthRule(1000),
            InspectionRollupRegionLengthRule(120),
        ),
    )
}
