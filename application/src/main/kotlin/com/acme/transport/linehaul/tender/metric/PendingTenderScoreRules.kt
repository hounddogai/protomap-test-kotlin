package com.acme.transport.linehaul.tender.metric

/** A rule a pending tender score must pass. Check returns its problems, or none. */
interface PendingTenderScoreRule {
    fun check(item: PendingTenderScore): MutableList<String>
}

data class PendingTenderScoreWeightRangeRule(var min: Double, var max: Double) : PendingTenderScoreRule {
    override fun check(item: PendingTenderScore): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderScoreActiveGuardRule(var limit: Double) : PendingTenderScoreRule {
    override fun check(item: PendingTenderScore): MutableList<String> {
        if (item.active && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderScoreNameLengthRule(var maxLength: Int) : PendingTenderScoreRule {
    override fun check(item: PendingTenderScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderScoreOwnerIdLengthRule(var maxLength: Int) : PendingTenderScoreRule {
    override fun check(item: PendingTenderScore): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderScoreChecksumLengthRule(var maxLength: Int) : PendingTenderScoreRule {
    override fun check(item: PendingTenderScore): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tender score. */
data class PendingTenderScoreValidator(var rules: MutableList<PendingTenderScoreRule>) {
    fun validate(item: PendingTenderScore): MutableList<String> {
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

    fun requireValid(item: PendingTenderScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTenderScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTenderScoreValidator(): PendingTenderScoreValidator {
    return PendingTenderScoreValidator(
        mutableListOf<PendingTenderScoreRule>(
            PendingTenderScoreWeightRangeRule(3.3, 169.4),
            PendingTenderScoreActiveGuardRule(6.7),
            PendingTenderScoreNameLengthRule(3600),
            PendingTenderScoreOwnerIdLengthRule(64),
            PendingTenderScoreChecksumLengthRule(3600),
        ),
    )
}
