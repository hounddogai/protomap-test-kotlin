package com.acme.transport.linehaul.laneplan.planagreement

/** A rule a primary lane plan draft must pass. Check returns its problems, or none. */
interface PrimaryLanePlanDraftRule {
    fun check(item: PrimaryLanePlanDraft): MutableList<String>
}

data class PrimaryLanePlanDraftQuantityRangeRule(var min: Int, var max: Int) : PrimaryLanePlanDraftRule {
    override fun check(item: PrimaryLanePlanDraft): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class PrimaryLanePlanDraftRequiredLabelRule(var message: String) : PrimaryLanePlanDraftRule {
    override fun check(item: PrimaryLanePlanDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLanePlanDraftLabelLengthRule(var maxLength: Int) : PrimaryLanePlanDraftRule {
    override fun check(item: PrimaryLanePlanDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLanePlanDraftRequiredChannelRule(var message: String) : PrimaryLanePlanDraftRule {
    override fun check(item: PrimaryLanePlanDraft): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLanePlanDraftCodePrefixRule(var prefix: String) : PrimaryLanePlanDraftRule {
    override fun check(item: PrimaryLanePlanDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLanePlanDraftCarrierLengthRule(var maxLength: Int) : PrimaryLanePlanDraftRule {
    override fun check(item: PrimaryLanePlanDraft): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary lane plan draft. */
data class PrimaryLanePlanDraftValidator(var rules: MutableList<PrimaryLanePlanDraftRule>) {
    fun validate(item: PrimaryLanePlanDraft): MutableList<String> {
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

    fun requireValid(item: PrimaryLanePlanDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryLanePlanDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryLanePlanDraftValidator(): PrimaryLanePlanDraftValidator {
    return PrimaryLanePlanDraftValidator(
        mutableListOf<PrimaryLanePlanDraftRule>(
            PrimaryLanePlanDraftQuantityRangeRule(3600, 86400),
            PrimaryLanePlanDraftRequiredLabelRule("primary lane plan draft needs this field"),
            PrimaryLanePlanDraftLabelLengthRule(80),
            PrimaryLanePlanDraftRequiredChannelRule("primary lane plan draft needs this field"),
            PrimaryLanePlanDraftCodePrefixRule("X"),
            PrimaryLanePlanDraftCarrierLengthRule(64),
        ),
    )
}
