package com.acme.transport.linehaul.laneplan.plansegment

/** A rule a lane plan claim must pass. Check returns its problems, or none. */
interface LanePlanClaimRule {
    fun check(item: LanePlanClaim): MutableList<String>
}

data class LanePlanClaimRequiredCodeRule(var message: String) : LanePlanClaimRule {
    override fun check(item: LanePlanClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanClaimTitleLengthRule(var maxLength: Int) : LanePlanClaimRule {
    override fun check(item: LanePlanClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanClaimRequiredTitleRule(var message: String) : LanePlanClaimRule {
    override fun check(item: LanePlanClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanClaimRequiredDepotRule(var message: String) : LanePlanClaimRule {
    override fun check(item: LanePlanClaim): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanClaimRequiredCountryRule(var message: String) : LanePlanClaimRule {
    override fun check(item: LanePlanClaim): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanClaimCountryLengthRule(var maxLength: Int) : LanePlanClaimRule {
    override fun check(item: LanePlanClaim): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan claim. */
data class LanePlanClaimValidator(var rules: MutableList<LanePlanClaimRule>) {
    fun validate(item: LanePlanClaim): MutableList<String> {
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

    fun requireValid(item: LanePlanClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanClaimValidator(): LanePlanClaimValidator {
    return LanePlanClaimValidator(
        mutableListOf<LanePlanClaimRule>(
            LanePlanClaimRequiredCodeRule("lane plan claim needs an entry"),
            LanePlanClaimTitleLengthRule(3600),
            LanePlanClaimRequiredTitleRule("lane plan claim needs a value"),
            LanePlanClaimRequiredDepotRule("lane plan claim needs an entry"),
            LanePlanClaimRequiredCountryRule("lane plan claim needs this field"),
            LanePlanClaimCountryLengthRule(120),
        ),
    )
}
