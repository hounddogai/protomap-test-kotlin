package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.text.normalize

/** A rule a primary proof of delivery schedule must pass. Check returns its problems, or none. */
interface PrimaryProofOfDeliveryScheduleRule {
    fun check(item: PrimaryProofOfDeliverySchedule): MutableList<String>
}

data class PrimaryProofOfDeliveryScheduleRequiredLabelRule(
    var message: String,
) : PrimaryProofOfDeliveryScheduleRule {
    override fun check(item: PrimaryProofOfDeliverySchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryScheduleRequiredCodeRule(
    var message: String,
) : PrimaryProofOfDeliveryScheduleRule {
    override fun check(item: PrimaryProofOfDeliverySchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryProofOfDeliveryScheduleRule {
    override fun check(item: PrimaryProofOfDeliverySchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryScheduleShiftCodeLengthRule(
    var maxLength: Int,
) : PrimaryProofOfDeliveryScheduleRule {
    override fun check(item: PrimaryProofOfDeliverySchedule): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary proof of delivery schedule. */
data class PrimaryProofOfDeliveryScheduleValidator(var rules: MutableList<PrimaryProofOfDeliveryScheduleRule>) {
    fun validate(item: PrimaryProofOfDeliverySchedule): MutableList<String> {
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

    fun requireValid(item: PrimaryProofOfDeliverySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryProofOfDeliverySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryProofOfDeliveryScheduleValidator(): PrimaryProofOfDeliveryScheduleValidator {
    return PrimaryProofOfDeliveryScheduleValidator(
        mutableListOf<PrimaryProofOfDeliveryScheduleRule>(
            PrimaryProofOfDeliveryScheduleRequiredLabelRule("primary proof of delivery schedule needs a value"),
            PrimaryProofOfDeliveryScheduleRequiredCodeRule(
                "primary proof of delivery schedule needs this field",
            ),
            PrimaryProofOfDeliveryScheduleAllowedCodeRule(mutableListOf<String>("scheduled", "scheduled")),
            PrimaryProofOfDeliveryScheduleShiftCodeLengthRule(32),
        ),
    )
}
