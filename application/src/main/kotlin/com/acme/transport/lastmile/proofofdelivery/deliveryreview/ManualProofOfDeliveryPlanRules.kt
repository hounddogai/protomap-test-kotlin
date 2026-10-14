package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.normalize

/** A rule a manual proof of delivery plan must pass. Check returns its problems, or none. */
interface ManualProofOfDeliveryPlanRule {
    fun check(item: ManualProofOfDeliveryPlan): MutableList<String>
}

data class ManualProofOfDeliveryPlanRequiredCodeRule(var message: String) : ManualProofOfDeliveryPlanRule {
    override fun check(item: ManualProofOfDeliveryPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryPlanCodeLengthRule(var maxLength: Int) : ManualProofOfDeliveryPlanRule {
    override fun check(item: ManualProofOfDeliveryPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryPlanTitleLengthRule(var maxLength: Int) : ManualProofOfDeliveryPlanRule {
    override fun check(item: ManualProofOfDeliveryPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryPlanAllowedTitleRule(
    var allowed: MutableList<String>,
) : ManualProofOfDeliveryPlanRule {
    override fun check(item: ManualProofOfDeliveryPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryPlanRevisionRangeRule(
    var min: Int,
    var max: Int,
) : ManualProofOfDeliveryPlanRule {
    override fun check(item: ManualProofOfDeliveryPlan): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual proof of delivery plan. */
data class ManualProofOfDeliveryPlanValidator(var rules: MutableList<ManualProofOfDeliveryPlanRule>) {
    fun validate(item: ManualProofOfDeliveryPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualProofOfDeliveryPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualProofOfDeliveryPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultManualProofOfDeliveryPlanValidator(): ManualProofOfDeliveryPlanValidator {
    return ManualProofOfDeliveryPlanValidator(
        mutableListOf<ManualProofOfDeliveryPlanRule>(
            ManualProofOfDeliveryPlanRequiredCodeRule("manual proof of delivery plan needs this field"),
            ManualProofOfDeliveryPlanCodeLengthRule(255),
            ManualProofOfDeliveryPlanTitleLengthRule(64),
            ManualProofOfDeliveryPlanAllowedTitleRule(mutableListOf<String>("scheduled", "tiered", "pending")),
            ManualProofOfDeliveryPlanRevisionRangeRule(32, 64),
        ),
    )
}
