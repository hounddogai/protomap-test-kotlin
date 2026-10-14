package com.acme.transport.lastmile.proofofdelivery.review

/** A rule a proof of delivery review must pass. Check returns its problems, or none. */
interface ProofOfDeliveryReviewRule {
    fun check(item: ProofOfDeliveryReview): MutableList<String>
}

data class ProofOfDeliveryReviewRequiredCodeRule(var message: String) : ProofOfDeliveryReviewRule {
    override fun check(item: ProofOfDeliveryReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryReviewRequiredDriverIdRule(var message: String) : ProofOfDeliveryReviewRule {
    override fun check(item: ProofOfDeliveryReview): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryReviewRequiredCurrencyRule(var message: String) : ProofOfDeliveryReviewRule {
    override fun check(item: ProofOfDeliveryReview): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryReviewRequiredNameRule(var message: String) : ProofOfDeliveryReviewRule {
    override fun check(item: ProofOfDeliveryReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery review. */
data class ProofOfDeliveryReviewValidator(var rules: MutableList<ProofOfDeliveryReviewRule>) {
    fun validate(item: ProofOfDeliveryReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryReview>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryReviewValidator(): ProofOfDeliveryReviewValidator {
    return ProofOfDeliveryReviewValidator(
        mutableListOf<ProofOfDeliveryReviewRule>(
            ProofOfDeliveryReviewRequiredCodeRule("proof of delivery review needs a value"),
            ProofOfDeliveryReviewRequiredDriverIdRule("proof of delivery review needs a value"),
            ProofOfDeliveryReviewRequiredCurrencyRule("proof of delivery review needs this field"),
            ProofOfDeliveryReviewRequiredNameRule("proof of delivery review needs a value"),
        ),
    )
}
