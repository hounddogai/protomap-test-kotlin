package com.acme.transport.fleetops.defect.notice

/** A rule a defect notice must pass. Check returns its problems, or none. */
interface DefectNoticeRule {
    fun check(item: DefectNotice): MutableList<String>
}

data class DefectNoticeLabelLengthRule(var maxLength: Int) : DefectNoticeRule {
    override fun check(item: DefectNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectNoticeCapacityRangeRule(var min: Int, var max: Int) : DefectNoticeRule {
    override fun check(item: DefectNotice): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectNoticeCodePrefixRule(var prefix: String) : DefectNoticeRule {
    override fun check(item: DefectNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect notice. */
data class DefectNoticeValidator(var rules: MutableList<DefectNoticeRule>) {
    fun validate(item: DefectNotice): MutableList<String> {
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

    fun requireValid(item: DefectNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectNoticeValidator(): DefectNoticeValidator {
    return DefectNoticeValidator(
        mutableListOf<DefectNoticeRule>(
            DefectNoticeLabelLengthRule(80),
            DefectNoticeCapacityRangeRule(64, 86400),
            DefectNoticeCodePrefixRule("OPS"),
        ),
    )
}
