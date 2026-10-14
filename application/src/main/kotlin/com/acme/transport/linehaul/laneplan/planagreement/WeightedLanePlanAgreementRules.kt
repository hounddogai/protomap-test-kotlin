package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.kit.text.normalize

/** A rule a weighted lane plan agreement must pass. Check returns its problems, or none. */
interface WeightedLanePlanAgreementRule {
    fun check(item: WeightedLanePlanAgreement): MutableList<String>
}

data class WeightedLanePlanAgreementAllowedPhoneRule(
    var allowed: MutableList<String>,
) : WeightedLanePlanAgreementRule {
    override fun check(item: WeightedLanePlanAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanAgreementRequiredLabelRule(var message: String) : WeightedLanePlanAgreementRule {
    override fun check(item: WeightedLanePlanAgreement): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanAgreementFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : WeightedLanePlanAgreementRule {
    override fun check(item: WeightedLanePlanAgreement): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedLanePlanAgreementPhoneLengthRule(var maxLength: Int) : WeightedLanePlanAgreementRule {
    override fun check(item: WeightedLanePlanAgreement): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted lane plan agreement. */
data class WeightedLanePlanAgreementValidator(var rules: MutableList<WeightedLanePlanAgreementRule>) {
    fun validate(item: WeightedLanePlanAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedLanePlanAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedLanePlanAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedLanePlanAgreementValidator(): WeightedLanePlanAgreementValidator {
    return WeightedLanePlanAgreementValidator(
        mutableListOf<WeightedLanePlanAgreementRule>(
            WeightedLanePlanAgreementAllowedPhoneRule(mutableListOf<String>("strict", "strict")),
            WeightedLanePlanAgreementRequiredLabelRule("weighted lane plan agreement needs this field"),
            WeightedLanePlanAgreementFloorValueRangeRule(2.1, 3255.0),
            WeightedLanePlanAgreementPhoneLengthRule(3600),
        ),
    )
}
