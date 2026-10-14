package com.acme.transport.fleetops.tire.manifest

/** A rule a tire notice must pass. Check returns its problems, or none. */
interface TireNoticeRule {
    fun check(item: TireNotice): MutableList<String>
}

data class TireNoticeSegmentLengthRule(var maxLength: Int) : TireNoticeRule {
    override fun check(item: TireNotice): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireNoticeRequiredCodeRule(var message: String) : TireNoticeRule {
    override fun check(item: TireNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireNoticeRequiredNameRule(var message: String) : TireNoticeRule {
    override fun check(item: TireNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireNoticeCodePrefixRule(var prefix: String) : TireNoticeRule {
    override fun check(item: TireNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireNoticeRatioRangeRule(var min: Double, var max: Double) : TireNoticeRule {
    override fun check(item: TireNotice): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tire notice. */
data class TireNoticeValidator(var rules: MutableList<TireNoticeRule>) {
    fun validate(item: TireNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultTireNoticeValidator(): TireNoticeValidator {
    return TireNoticeValidator(
        mutableListOf<TireNoticeRule>(
            TireNoticeSegmentLengthRule(86400),
            TireNoticeRequiredCodeRule("tire notice needs this field"),
            TireNoticeRequiredNameRule("tire notice needs this field"),
            TireNoticeCodePrefixRule("OPS"),
            TireNoticeRatioRangeRule(3.3, 1785.6),
        ),
    )
}
