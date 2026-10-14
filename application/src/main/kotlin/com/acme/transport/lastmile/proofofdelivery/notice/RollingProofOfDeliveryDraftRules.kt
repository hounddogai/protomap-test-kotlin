package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.text.normalize

/** A rule a rolling proof of delivery draft must pass. Check returns its problems, or none. */
interface RollingProofOfDeliveryDraftRule {
    fun check(item: RollingProofOfDeliveryDraft): MutableList<String>
}

data class RollingProofOfDeliveryDraftCodeLengthRule(var maxLength: Int) : RollingProofOfDeliveryDraftRule {
    override fun check(item: RollingProofOfDeliveryDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryDraftRequiredCodeRule(var message: String) : RollingProofOfDeliveryDraftRule {
    override fun check(item: RollingProofOfDeliveryDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryDraftAllowedNameRule(
    var allowed: MutableList<String>,
) : RollingProofOfDeliveryDraftRule {
    override fun check(item: RollingProofOfDeliveryDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling proof of delivery draft. */
data class RollingProofOfDeliveryDraftValidator(var rules: MutableList<RollingProofOfDeliveryDraftRule>) {
    fun validate(item: RollingProofOfDeliveryDraft): MutableList<String> {
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

    fun requireValid(item: RollingProofOfDeliveryDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingProofOfDeliveryDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingProofOfDeliveryDraftValidator(): RollingProofOfDeliveryDraftValidator {
    return RollingProofOfDeliveryDraftValidator(
        mutableListOf<RollingProofOfDeliveryDraftRule>(
            RollingProofOfDeliveryDraftCodeLengthRule(80),
            RollingProofOfDeliveryDraftRequiredCodeRule("rolling proof of delivery draft needs a value"),
            RollingProofOfDeliveryDraftAllowedNameRule(mutableListOf<String>("weighted")),
        ),
    )
}
