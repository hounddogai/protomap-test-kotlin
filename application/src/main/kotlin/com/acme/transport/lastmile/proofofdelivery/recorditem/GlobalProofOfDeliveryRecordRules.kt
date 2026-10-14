package com.acme.transport.lastmile.proofofdelivery.recorditem

/** A rule a global proof of delivery record must pass. Check returns its problems, or none. */
interface GlobalProofOfDeliveryRecordRule {
    fun check(item: GlobalProofOfDeliveryRecord): MutableList<String>
}

data class GlobalProofOfDeliveryRecordAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : GlobalProofOfDeliveryRecordRule {
    override fun check(item: GlobalProofOfDeliveryRecord): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class GlobalProofOfDeliveryRecordActiveGuardRule(var limit: Int) : GlobalProofOfDeliveryRecordRule {
    override fun check(item: GlobalProofOfDeliveryRecord): MutableList<String> {
        if (item.active && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryRecordRequiredTitleRule(var message: String) : GlobalProofOfDeliveryRecordRule {
    override fun check(item: GlobalProofOfDeliveryRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryRecordTitleLengthRule(var maxLength: Int) : GlobalProofOfDeliveryRecordRule {
    override fun check(item: GlobalProofOfDeliveryRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryRecordRequiredCategoryRule(
    var message: String,
) : GlobalProofOfDeliveryRecordRule {
    override fun check(item: GlobalProofOfDeliveryRecord): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalProofOfDeliveryRecordOdometerRangeRule(
    var min: Int,
    var max: Int,
) : GlobalProofOfDeliveryRecordRule {
    override fun check(item: GlobalProofOfDeliveryRecord): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global proof of delivery record. */
data class GlobalProofOfDeliveryRecordValidator(var rules: MutableList<GlobalProofOfDeliveryRecordRule>) {
    fun validate(item: GlobalProofOfDeliveryRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalProofOfDeliveryRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalProofOfDeliveryRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalProofOfDeliveryRecordValidator(): GlobalProofOfDeliveryRecordValidator {
    return GlobalProofOfDeliveryRecordValidator(
        mutableListOf<GlobalProofOfDeliveryRecordRule>(
            GlobalProofOfDeliveryRecordAttemptsRangeRule(16, 80),
            GlobalProofOfDeliveryRecordActiveGuardRule(1000),
            GlobalProofOfDeliveryRecordRequiredTitleRule("global proof of delivery record needs a value"),
            GlobalProofOfDeliveryRecordTitleLengthRule(32),
            GlobalProofOfDeliveryRecordRequiredCategoryRule("global proof of delivery record needs a value"),
            GlobalProofOfDeliveryRecordOdometerRangeRule(120, 86400),
        ),
    )
}
