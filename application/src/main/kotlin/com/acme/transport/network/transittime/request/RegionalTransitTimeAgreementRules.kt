package com.acme.transport.network.transittime.request

/** A rule a regional transit time agreement must pass. Check returns its problems, or none. */
interface RegionalTransitTimeAgreementRule {
    fun check(item: RegionalTransitTimeAgreement): MutableList<String>
}

data class RegionalTransitTimeAgreementRequiredCodeRule(
    var message: String,
) : RegionalTransitTimeAgreementRule {
    override fun check(item: RegionalTransitTimeAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTransitTimeAgreementRequiredNameRule(
    var message: String,
) : RegionalTransitTimeAgreementRule {
    override fun check(item: RegionalTransitTimeAgreement): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTransitTimeAgreementRequiredReviewerRule(
    var message: String,
) : RegionalTransitTimeAgreementRule {
    override fun check(item: RegionalTransitTimeAgreement): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalTransitTimeAgreementVisibleGuardRule(var limit: Double) : RegionalTransitTimeAgreementRule {
    override fun check(item: RegionalTransitTimeAgreement): MutableList<String> {
        if (item.visible && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional transit time agreement. */
data class RegionalTransitTimeAgreementValidator(var rules: MutableList<RegionalTransitTimeAgreementRule>) {
    fun validate(item: RegionalTransitTimeAgreement): MutableList<String> {
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

    fun requireValid(item: RegionalTransitTimeAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTransitTimeAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTransitTimeAgreementValidator(): RegionalTransitTimeAgreementValidator {
    return RegionalTransitTimeAgreementValidator(
        mutableListOf<RegionalTransitTimeAgreementRule>(
            RegionalTransitTimeAgreementRequiredCodeRule("regional transit time agreement needs this field"),
            RegionalTransitTimeAgreementRequiredNameRule("regional transit time agreement needs this field"),
            RegionalTransitTimeAgreementRequiredReviewerRule("regional transit time agreement needs a value"),
            RegionalTransitTimeAgreementVisibleGuardRule(4.0),
        ),
    )
}
