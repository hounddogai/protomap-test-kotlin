package com.acme.transport.customs.inspection.allocation

/** A rule a pending inspection rollup must pass. Check returns its problems, or none. */
interface PendingInspectionRollupRule {
    fun check(item: PendingInspectionRollup): MutableList<String>
}

data class PendingInspectionRollupRequiredTrailerIdRule(var message: String) : PendingInspectionRollupRule {
    override fun check(item: PendingInspectionRollup): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionRollupLabelLengthRule(var maxLength: Int) : PendingInspectionRollupRule {
    override fun check(item: PendingInspectionRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionRollupCodeLengthRule(var maxLength: Int) : PendingInspectionRollupRule {
    override fun check(item: PendingInspectionRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionRollupWeightRangeRule(
    var min: Double,
    var max: Double,
) : PendingInspectionRollupRule {
    override fun check(item: PendingInspectionRollup): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection rollup. */
data class PendingInspectionRollupValidator(var rules: MutableList<PendingInspectionRollupRule>) {
    fun validate(item: PendingInspectionRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingInspectionRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionRollupValidator(): PendingInspectionRollupValidator {
    return PendingInspectionRollupValidator(
        mutableListOf<PendingInspectionRollupRule>(
            PendingInspectionRollupRequiredTrailerIdRule("pending inspection rollup needs an entry"),
            PendingInspectionRollupLabelLengthRule(32),
            PendingInspectionRollupCodeLengthRule(86400),
            PendingInspectionRollupWeightRangeRule(8.7, 3083.7),
        ),
    )
}
