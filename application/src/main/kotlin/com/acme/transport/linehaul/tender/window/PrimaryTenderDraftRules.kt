package com.acme.transport.linehaul.tender.window

/** A rule a primary tender draft must pass. Check returns its problems, or none. */
interface PrimaryTenderDraftRule {
    fun check(item: PrimaryTenderDraft): MutableList<String>
}

data class PrimaryTenderDraftRequiredTitleRule(var message: String) : PrimaryTenderDraftRule {
    override fun check(item: PrimaryTenderDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderDraftRequiredRouteCodeRule(var message: String) : PrimaryTenderDraftRule {
    override fun check(item: PrimaryTenderDraft): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderDraftRouteCodeLengthRule(var maxLength: Int) : PrimaryTenderDraftRule {
    override fun check(item: PrimaryTenderDraft): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderDraftRequiredCodeRule(var message: String) : PrimaryTenderDraftRule {
    override fun check(item: PrimaryTenderDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderDraftCostRangeRule(var min: Double, var max: Double) : PrimaryTenderDraftRule {
    override fun check(item: PrimaryTenderDraft): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary tender draft. */
data class PrimaryTenderDraftValidator(var rules: MutableList<PrimaryTenderDraftRule>) {
    fun validate(item: PrimaryTenderDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTenderDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTenderDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTenderDraftValidator(): PrimaryTenderDraftValidator {
    return PrimaryTenderDraftValidator(
        mutableListOf<PrimaryTenderDraftRule>(
            PrimaryTenderDraftRequiredTitleRule("primary tender draft needs this field"),
            PrimaryTenderDraftRequiredRouteCodeRule("primary tender draft needs this field"),
            PrimaryTenderDraftRouteCodeLengthRule(255),
            PrimaryTenderDraftRequiredCodeRule("primary tender draft needs this field"),
            PrimaryTenderDraftCostRangeRule(0.3, 3121.0),
        ),
    )
}
