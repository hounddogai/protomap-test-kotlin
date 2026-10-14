package com.acme.transport.network.node.weightednodeagreement

/** A rule a global node agreement must pass. Check returns its problems, or none. */
interface GlobalNodeAgreementRule {
    fun check(item: GlobalNodeAgreement): MutableList<String>
}

data class GlobalNodeAgreementCategoryLengthRule(var maxLength: Int) : GlobalNodeAgreementRule {
    override fun check(item: GlobalNodeAgreement): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeAgreementRequiredCategoryRule(var message: String) : GlobalNodeAgreementRule {
    override fun check(item: GlobalNodeAgreement): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeAgreementNameLengthRule(var maxLength: Int) : GlobalNodeAgreementRule {
    override fun check(item: GlobalNodeAgreement): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeAgreementEtaMinutesRangeRule(var min: Int, var max: Int) : GlobalNodeAgreementRule {
    override fun check(item: GlobalNodeAgreement): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global node agreement. */
data class GlobalNodeAgreementValidator(var rules: MutableList<GlobalNodeAgreementRule>) {
    fun validate(item: GlobalNodeAgreement): MutableList<String> {
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

    fun requireValid(item: GlobalNodeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalNodeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalNodeAgreementValidator(): GlobalNodeAgreementValidator {
    return GlobalNodeAgreementValidator(
        mutableListOf<GlobalNodeAgreementRule>(
            GlobalNodeAgreementCategoryLengthRule(32),
            GlobalNodeAgreementRequiredCategoryRule("global node agreement needs a value"),
            GlobalNodeAgreementNameLengthRule(3600),
            GlobalNodeAgreementEtaMinutesRangeRule(3600, 3600),
        ),
    )
}
