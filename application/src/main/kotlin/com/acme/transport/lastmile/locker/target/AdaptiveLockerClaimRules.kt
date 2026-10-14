package com.acme.transport.lastmile.locker.target

/** A rule a adaptive locker claim must pass. Check returns its problems, or none. */
interface AdaptiveLockerClaimRule {
    fun check(item: AdaptiveLockerClaim): MutableList<String>
}

data class AdaptiveLockerClaimRequiredLaneRule(var message: String) : AdaptiveLockerClaimRule {
    override fun check(item: AdaptiveLockerClaim): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerClaimRequiredLabelRule(var message: String) : AdaptiveLockerClaimRule {
    override fun check(item: AdaptiveLockerClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerClaimRequiredDepotRule(var message: String) : AdaptiveLockerClaimRule {
    override fun check(item: AdaptiveLockerClaim): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveLockerClaimLaneLengthRule(var maxLength: Int) : AdaptiveLockerClaimRule {
    override fun check(item: AdaptiveLockerClaim): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive locker claim. */
data class AdaptiveLockerClaimValidator(var rules: MutableList<AdaptiveLockerClaimRule>) {
    fun validate(item: AdaptiveLockerClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveLockerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveLockerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveLockerClaimValidator(): AdaptiveLockerClaimValidator {
    return AdaptiveLockerClaimValidator(
        mutableListOf<AdaptiveLockerClaimRule>(
            AdaptiveLockerClaimRequiredLaneRule("adaptive locker claim needs a value"),
            AdaptiveLockerClaimRequiredLabelRule("adaptive locker claim needs an entry"),
            AdaptiveLockerClaimRequiredDepotRule("adaptive locker claim needs this field"),
            AdaptiveLockerClaimLaneLengthRule(64),
        ),
    )
}
