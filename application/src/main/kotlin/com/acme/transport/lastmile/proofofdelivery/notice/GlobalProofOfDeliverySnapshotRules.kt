package com.acme.transport.lastmile.proofofdelivery.notice

/** A rule a global proof of delivery snapshot must pass. Check returns its problems, or none. */
interface GlobalProofOfDeliverySnapshotRule {
    fun check(item: GlobalProofOfDeliverySnapshot): MutableList<String>
}

data class GlobalProofOfDeliverySnapshotLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : GlobalProofOfDeliverySnapshotRule {
    override fun check(item: GlobalProofOfDeliverySnapshot): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class GlobalProofOfDeliverySnapshotCodeLengthRule(var maxLength: Int) : GlobalProofOfDeliverySnapshotRule {
    override fun check(item: GlobalProofOfDeliverySnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliverySnapshotRequiredCodeRule(
    var message: String,
) : GlobalProofOfDeliverySnapshotRule {
    override fun check(item: GlobalProofOfDeliverySnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global proof of delivery snapshot. */
data class GlobalProofOfDeliverySnapshotValidator(var rules: MutableList<GlobalProofOfDeliverySnapshotRule>) {
    fun validate(item: GlobalProofOfDeliverySnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalProofOfDeliverySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalProofOfDeliverySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalProofOfDeliverySnapshotValidator(): GlobalProofOfDeliverySnapshotValidator {
    return GlobalProofOfDeliverySnapshotValidator(
        mutableListOf<GlobalProofOfDeliverySnapshotRule>(
            GlobalProofOfDeliverySnapshotLoadKgRangeRule(3.4, 3188.6),
            GlobalProofOfDeliverySnapshotCodeLengthRule(16),
            GlobalProofOfDeliverySnapshotRequiredCodeRule("global proof of delivery snapshot needs this field"),
        ),
    )
}
