package com.acme.transport.fleetops.leasereturn.rule

/** A rule a lease return mapping must pass. Check returns its problems, or none. */
interface LeaseReturnMappingRule {
    fun check(item: LeaseReturnMapping): MutableList<String>
}

data class LeaseReturnMappingCodeLengthRule(var maxLength: Int) : LeaseReturnMappingRule {
    override fun check(item: LeaseReturnMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMappingRequiredDriverIdRule(var message: String) : LeaseReturnMappingRule {
    override fun check(item: LeaseReturnMapping): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMappingRequiredCountryRule(var message: String) : LeaseReturnMappingRule {
    override fun check(item: LeaseReturnMapping): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMappingLabelLengthRule(var maxLength: Int) : LeaseReturnMappingRule {
    override fun check(item: LeaseReturnMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnMappingRequiredLabelRule(var message: String) : LeaseReturnMappingRule {
    override fun check(item: LeaseReturnMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return mapping. */
data class LeaseReturnMappingValidator(var rules: MutableList<LeaseReturnMappingRule>) {
    fun validate(item: LeaseReturnMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnMappingValidator(): LeaseReturnMappingValidator {
    return LeaseReturnMappingValidator(
        mutableListOf<LeaseReturnMappingRule>(
            LeaseReturnMappingCodeLengthRule(64),
            LeaseReturnMappingRequiredDriverIdRule("lease return mapping needs an entry"),
            LeaseReturnMappingRequiredCountryRule("lease return mapping needs this field"),
            LeaseReturnMappingLabelLengthRule(3600),
            LeaseReturnMappingRequiredLabelRule("lease return mapping needs a value"),
        ),
    )
}
