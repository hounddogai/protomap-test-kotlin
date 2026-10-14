package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.text.normalize

/** A rule a fallback qualification snapshot must pass. Check returns its problems, or none. */
interface FallbackQualificationSnapshotRule {
    fun check(item: FallbackQualificationSnapshot): MutableList<String>
}

data class FallbackQualificationSnapshotRequiredCodeRule(
    var message: String,
) : FallbackQualificationSnapshotRule {
    override fun check(item: FallbackQualificationSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSnapshotArchivedGuardRule(var limit: Int) : FallbackQualificationSnapshotRule {
    override fun check(item: FallbackQualificationSnapshot): MutableList<String> {
        if (item.archived && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSnapshotRequiredLocaleRule(
    var message: String,
) : FallbackQualificationSnapshotRule {
    override fun check(item: FallbackQualificationSnapshot): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSnapshotStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackQualificationSnapshotRule {
    override fun check(item: FallbackQualificationSnapshot): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSnapshotAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : FallbackQualificationSnapshotRule {
    override fun check(item: FallbackQualificationSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is too long")
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSnapshotLocaleLengthRule(
    var maxLength: Int,
) : FallbackQualificationSnapshotRule {
    override fun check(item: FallbackQualificationSnapshot): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback qualification snapshot. */
data class FallbackQualificationSnapshotValidator(var rules: MutableList<FallbackQualificationSnapshotRule>) {
    fun validate(item: FallbackQualificationSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackQualificationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackQualificationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackQualificationSnapshotValidator(): FallbackQualificationSnapshotValidator {
    return FallbackQualificationSnapshotValidator(
        mutableListOf<FallbackQualificationSnapshotRule>(
            FallbackQualificationSnapshotRequiredCodeRule("fallback qualification snapshot needs an entry"),
            FallbackQualificationSnapshotArchivedGuardRule(64),
            FallbackQualificationSnapshotRequiredLocaleRule("fallback qualification snapshot needs an entry"),
            FallbackQualificationSnapshotStartsAtRangeRule(16, 3600),
            FallbackQualificationSnapshotAllowedDriverIdRule(mutableListOf<String>("weighted", "global")),
            FallbackQualificationSnapshotLocaleLengthRule(80),
        ),
    )
}
