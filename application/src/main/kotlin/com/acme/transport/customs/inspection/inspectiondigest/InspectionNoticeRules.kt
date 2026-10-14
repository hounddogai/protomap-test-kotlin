package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.text.normalize

/** A rule a inspection notice must pass. Check returns its problems, or none. */
interface InspectionNoticeRule {
    fun check(item: InspectionNotice): MutableList<String>
}

data class InspectionNoticeRequiredCountryRule(var message: String) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionNoticeCodeLengthRule(var maxLength: Int) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionNoticeRequiredCodeRule(var message: String) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionNoticeAllowedCountryRule(var allowed: MutableList<String>) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class InspectionNoticeCodePrefixRule(var prefix: String) : InspectionNoticeRule {
    override fun check(item: InspectionNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection notice. */
data class InspectionNoticeValidator(var rules: MutableList<InspectionNoticeRule>) {
    fun validate(item: InspectionNotice): MutableList<String> {
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

    fun requireValid(item: InspectionNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionNoticeValidator(): InspectionNoticeValidator {
    return InspectionNoticeValidator(
        mutableListOf<InspectionNoticeRule>(
            InspectionNoticeRequiredCountryRule("inspection notice needs an entry"),
            InspectionNoticeCodeLengthRule(120),
            InspectionNoticeRequiredCodeRule("inspection notice needs a value"),
            InspectionNoticeAllowedCountryRule(mutableListOf<String>("global")),
            InspectionNoticeCodePrefixRule("REF"),
        ),
    )
}
