package com.acme.transport.fleetops.serviceplan.forecast

/** A rule a capped service plan entry must pass. Check returns its problems, or none. */
interface CappedServicePlanEntryRule {
    fun check(item: CappedServicePlanEntry): MutableList<String>
}

data class CappedServicePlanEntryCodeLengthRule(var maxLength: Int) : CappedServicePlanEntryRule {
    override fun check(item: CappedServicePlanEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedServicePlanEntryDockLengthRule(var maxLength: Int) : CappedServicePlanEntryRule {
    override fun check(item: CappedServicePlanEntry): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedServicePlanEntryRequiredCodeRule(var message: String) : CappedServicePlanEntryRule {
    override fun check(item: CappedServicePlanEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedServicePlanEntryNameLengthRule(var maxLength: Int) : CappedServicePlanEntryRule {
    override fun check(item: CappedServicePlanEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped service plan entry. */
data class CappedServicePlanEntryValidator(var rules: MutableList<CappedServicePlanEntryRule>) {
    fun validate(item: CappedServicePlanEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedServicePlanEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedServicePlanEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedServicePlanEntryValidator(): CappedServicePlanEntryValidator {
    return CappedServicePlanEntryValidator(
        mutableListOf<CappedServicePlanEntryRule>(
            CappedServicePlanEntryCodeLengthRule(3600),
            CappedServicePlanEntryDockLengthRule(16),
            CappedServicePlanEntryRequiredCodeRule("capped service plan entry needs an entry"),
            CappedServicePlanEntryNameLengthRule(64),
        ),
    )
}
