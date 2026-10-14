package com.acme.transport.lastmile.proofofdelivery.assignment

/** A rule a proof of delivery schedule must pass. Check returns its problems, or none. */
interface ProofOfDeliveryScheduleRule {
    fun check(item: ProofOfDeliverySchedule): MutableList<String>
}

data class ProofOfDeliveryScheduleRequiredLocaleRule(var message: String) : ProofOfDeliveryScheduleRule {
    override fun check(item: ProofOfDeliverySchedule): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryScheduleDescriptionLengthRule(var maxLength: Int) : ProofOfDeliveryScheduleRule {
    override fun check(item: ProofOfDeliverySchedule): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryScheduleVisibleGuardRule(var limit: Int) : ProofOfDeliveryScheduleRule {
    override fun check(item: ProofOfDeliverySchedule): MutableList<String> {
        if (item.visible && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery schedule. */
data class ProofOfDeliveryScheduleValidator(var rules: MutableList<ProofOfDeliveryScheduleRule>) {
    fun validate(item: ProofOfDeliverySchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliverySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliverySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryScheduleValidator(): ProofOfDeliveryScheduleValidator {
    return ProofOfDeliveryScheduleValidator(
        mutableListOf<ProofOfDeliveryScheduleRule>(
            ProofOfDeliveryScheduleRequiredLocaleRule("proof of delivery schedule needs an entry"),
            ProofOfDeliveryScheduleDescriptionLengthRule(86400),
            ProofOfDeliveryScheduleVisibleGuardRule(32),
        ),
    )
}
