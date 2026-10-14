package com.acme.transport.safety.nearmiss.plan

/** A rule a near miss plan must pass. Check returns its problems, or none. */
interface NearMissPlanRule {
    fun check(item: NearMissPlan): MutableList<String>
}

data class NearMissPlanRequiredDockRule(var message: String) : NearMissPlanRule {
    override fun check(item: NearMissPlan): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissPlanDockLengthRule(var maxLength: Int) : NearMissPlanRule {
    override fun check(item: NearMissPlan): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissPlanRequiredTitleRule(var message: String) : NearMissPlanRule {
    override fun check(item: NearMissPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss plan. */
data class NearMissPlanValidator(var rules: MutableList<NearMissPlanRule>) {
    fun validate(item: NearMissPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissPlanValidator(): NearMissPlanValidator {
    return NearMissPlanValidator(
        mutableListOf<NearMissPlanRule>(
            NearMissPlanRequiredDockRule("near miss plan needs a value"),
            NearMissPlanDockLengthRule(32),
            NearMissPlanRequiredTitleRule("near miss plan needs an entry"),
        ),
    )
}
