package com.acme.transport.linehaul.laneplan.item

/** A rule a lane plan item must pass. Check returns its problems, or none. */
interface LanePlanItemRule {
    fun check(item: LanePlanItem): MutableList<String>
}

data class LanePlanItemSegmentLengthRule(var maxLength: Int) : LanePlanItemRule {
    override fun check(item: LanePlanItem): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanItemRequiredSegmentRule(var message: String) : LanePlanItemRule {
    override fun check(item: LanePlanItem): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanItemCodeLengthRule(var maxLength: Int) : LanePlanItemRule {
    override fun check(item: LanePlanItem): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan item. */
data class LanePlanItemValidator(var rules: MutableList<LanePlanItemRule>) {
    fun validate(item: LanePlanItem): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanItem) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanItem>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanItemValidator(): LanePlanItemValidator {
    return LanePlanItemValidator(
        mutableListOf<LanePlanItemRule>(
            LanePlanItemSegmentLengthRule(120),
            LanePlanItemRequiredSegmentRule("lane plan item needs an entry"),
            LanePlanItemCodeLengthRule(32),
        ),
    )
}
