package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.text.normalize

/** A rule a defect plan must pass. Check returns its problems, or none. */
interface DefectPlanRule {
    fun check(item: DefectPlan): MutableList<String>
}

data class DefectPlanAllowedNameRule(var allowed: MutableList<String>) : DefectPlanRule {
    override fun check(item: DefectPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class DefectPlanCodePrefixRule(var prefix: String) : DefectPlanRule {
    override fun check(item: DefectPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectPlanRequiredRouteCodeRule(var message: String) : DefectPlanRule {
    override fun check(item: DefectPlan): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectPlanRequiredNameRule(var message: String) : DefectPlanRule {
    override fun check(item: DefectPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectPlanTitleLengthRule(var maxLength: Int) : DefectPlanRule {
    override fun check(item: DefectPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectPlanRequiredCodeRule(var message: String) : DefectPlanRule {
    override fun check(item: DefectPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect plan. */
data class DefectPlanValidator(var rules: MutableList<DefectPlanRule>) {
    fun validate(item: DefectPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectPlanValidator(): DefectPlanValidator {
    return DefectPlanValidator(
        mutableListOf<DefectPlanRule>(
            DefectPlanAllowedNameRule(mutableListOf<String>("seasonal", "default")),
            DefectPlanCodePrefixRule("REF"),
            DefectPlanRequiredRouteCodeRule("defect plan needs an entry"),
            DefectPlanRequiredNameRule("defect plan needs a value"),
            DefectPlanTitleLengthRule(86400),
            DefectPlanRequiredCodeRule("defect plan needs an entry"),
        ),
    )
}
