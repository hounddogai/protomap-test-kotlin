package com.acme.transport.linehaul.backhaul.backhaulallocation

/** A rule a backhaul segment must pass. Check returns its problems, or none. */
interface BackhaulSegmentRule {
    fun check(item: BackhaulSegment): MutableList<String>
}

data class BackhaulSegmentRequiredLabelRule(var message: String) : BackhaulSegmentRule {
    override fun check(item: BackhaulSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSegmentCodeLengthRule(var maxLength: Int) : BackhaulSegmentRule {
    override fun check(item: BackhaulSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulSegmentLabelLengthRule(var maxLength: Int) : BackhaulSegmentRule {
    override fun check(item: BackhaulSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul segment. */
data class BackhaulSegmentValidator(var rules: MutableList<BackhaulSegmentRule>) {
    fun validate(item: BackhaulSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulSegmentValidator(): BackhaulSegmentValidator {
    return BackhaulSegmentValidator(
        mutableListOf<BackhaulSegmentRule>(
            BackhaulSegmentRequiredLabelRule("backhaul segment needs this field"),
            BackhaulSegmentCodeLengthRule(86400),
            BackhaulSegmentLabelLengthRule(86400),
        ),
    )
}
