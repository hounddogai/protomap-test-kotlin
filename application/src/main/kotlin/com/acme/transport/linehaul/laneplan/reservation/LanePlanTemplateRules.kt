package com.acme.transport.linehaul.laneplan.reservation

/** A rule a lane plan template must pass. Check returns its problems, or none. */
interface LanePlanTemplateRule {
    fun check(item: LanePlanTemplate): MutableList<String>
}

data class LanePlanTemplateChecksumLengthRule(var maxLength: Int) : LanePlanTemplateRule {
    override fun check(item: LanePlanTemplate): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanTemplateRequiredLabelRule(var message: String) : LanePlanTemplateRule {
    override fun check(item: LanePlanTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanTemplateVisibleGuardRule(var limit: Double) : LanePlanTemplateRule {
    override fun check(item: LanePlanTemplate): MutableList<String> {
        if (item.visible && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanTemplateCodePrefixRule(var prefix: String) : LanePlanTemplateRule {
    override fun check(item: LanePlanTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan template. */
data class LanePlanTemplateValidator(var rules: MutableList<LanePlanTemplateRule>) {
    fun validate(item: LanePlanTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanTemplateValidator(): LanePlanTemplateValidator {
    return LanePlanTemplateValidator(
        mutableListOf<LanePlanTemplateRule>(
            LanePlanTemplateChecksumLengthRule(1000),
            LanePlanTemplateRequiredLabelRule("lane plan template needs a value"),
            LanePlanTemplateVisibleGuardRule(0.4),
            LanePlanTemplateCodePrefixRule("OPS"),
        ),
    )
}
