package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.text.normalize

/** A rule a rolling redelivery template must pass. Check returns its problems, or none. */
interface RollingRedeliveryTemplateRule {
    fun check(item: RollingRedeliveryTemplate): MutableList<String>
}

data class RollingRedeliveryTemplateAllowedLabelRule(
    var allowed: MutableList<String>,
) : RollingRedeliveryTemplateRule {
    override fun check(item: RollingRedeliveryTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RollingRedeliveryTemplateRequiredLabelRule(var message: String) : RollingRedeliveryTemplateRule {
    override fun check(item: RollingRedeliveryTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRedeliveryTemplateRequiredLaneRule(var message: String) : RollingRedeliveryTemplateRule {
    override fun check(item: RollingRedeliveryTemplate): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRedeliveryTemplateLabelLengthRule(var maxLength: Int) : RollingRedeliveryTemplateRule {
    override fun check(item: RollingRedeliveryTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRedeliveryTemplateLaneLengthRule(var maxLength: Int) : RollingRedeliveryTemplateRule {
    override fun check(item: RollingRedeliveryTemplate): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingRedeliveryTemplateRatioRangeRule(
    var min: Double,
    var max: Double,
) : RollingRedeliveryTemplateRule {
    override fun check(item: RollingRedeliveryTemplate): MutableList<String> {
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

/** Runs every rule of a rolling redelivery template. */
data class RollingRedeliveryTemplateValidator(var rules: MutableList<RollingRedeliveryTemplateRule>) {
    fun validate(item: RollingRedeliveryTemplate): MutableList<String> {
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

    fun requireValid(item: RollingRedeliveryTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRedeliveryTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRedeliveryTemplateValidator(): RollingRedeliveryTemplateValidator {
    return RollingRedeliveryTemplateValidator(
        mutableListOf<RollingRedeliveryTemplateRule>(
            RollingRedeliveryTemplateAllowedLabelRule(mutableListOf<String>("strict")),
            RollingRedeliveryTemplateRequiredLabelRule("rolling redelivery template needs this field"),
            RollingRedeliveryTemplateRequiredLaneRule("rolling redelivery template needs this field"),
            RollingRedeliveryTemplateLabelLengthRule(1000),
            RollingRedeliveryTemplateLaneLengthRule(16),
            RollingRedeliveryTemplateRatioRangeRule(1.9, 1933.0),
        ),
    )
}
