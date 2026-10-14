package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize

/** A rule a legacy inspection rule must pass. Check returns its problems, or none. */
interface LegacyInspectionRuleRule {
    fun check(item: LegacyInspectionRule): MutableList<String>
}

data class LegacyInspectionRuleAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : LegacyInspectionRuleRule {
    override fun check(item: LegacyInspectionRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionRuleRequiredDockRule(var message: String) : LegacyInspectionRuleRule {
    override fun check(item: LegacyInspectionRule): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionRuleNameLengthRule(var maxLength: Int) : LegacyInspectionRuleRule {
    override fun check(item: LegacyInspectionRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionRuleCodePrefixRule(var prefix: String) : LegacyInspectionRuleRule {
    override fun check(item: LegacyInspectionRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection rule. */
data class LegacyInspectionRuleValidator(var rules: MutableList<LegacyInspectionRuleRule>) {
    fun validate(item: LegacyInspectionRule): MutableList<String> {
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

    fun requireValid(item: LegacyInspectionRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionRuleValidator(): LegacyInspectionRuleValidator {
    return LegacyInspectionRuleValidator(
        mutableListOf<LegacyInspectionRuleRule>(
            LegacyInspectionRuleAllowedShiftCodeRule(mutableListOf<String>("legacy", "lenient")),
            LegacyInspectionRuleRequiredDockRule("legacy inspection rule needs a value"),
            LegacyInspectionRuleNameLengthRule(16),
            LegacyInspectionRuleCodePrefixRule("OPS"),
        ),
    )
}
