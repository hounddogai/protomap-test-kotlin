package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.text.normalize

/** A rule a manual proof of delivery record must pass. Check returns its problems, or none. */
interface ManualProofOfDeliveryRecordRule {
    fun check(item: ManualProofOfDeliveryRecord): MutableList<String>
}

data class ManualProofOfDeliveryRecordCodeLengthRule(var maxLength: Int) : ManualProofOfDeliveryRecordRule {
    override fun check(item: ManualProofOfDeliveryRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryRecordTitleLengthRule(var maxLength: Int) : ManualProofOfDeliveryRecordRule {
    override fun check(item: ManualProofOfDeliveryRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryRecordRequiredSourceRule(
    var message: String,
) : ManualProofOfDeliveryRecordRule {
    override fun check(item: ManualProofOfDeliveryRecord): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryRecordCodePrefixRule(var prefix: String) : ManualProofOfDeliveryRecordRule {
    override fun check(item: ManualProofOfDeliveryRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryRecordAllowedTitleRule(
    var allowed: MutableList<String>,
) : ManualProofOfDeliveryRecordRule {
    override fun check(item: ManualProofOfDeliveryRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual proof of delivery record. */
data class ManualProofOfDeliveryRecordValidator(var rules: MutableList<ManualProofOfDeliveryRecordRule>) {
    fun validate(item: ManualProofOfDeliveryRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualProofOfDeliveryRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualProofOfDeliveryRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultManualProofOfDeliveryRecordValidator(): ManualProofOfDeliveryRecordValidator {
    return ManualProofOfDeliveryRecordValidator(
        mutableListOf<ManualProofOfDeliveryRecordRule>(
            ManualProofOfDeliveryRecordCodeLengthRule(255),
            ManualProofOfDeliveryRecordTitleLengthRule(86400),
            ManualProofOfDeliveryRecordRequiredSourceRule("manual proof of delivery record needs an entry"),
            ManualProofOfDeliveryRecordCodePrefixRule("REF"),
            ManualProofOfDeliveryRecordAllowedTitleRule(mutableListOf<String>("strict", "legacy", "batched")),
        ),
    )
}
