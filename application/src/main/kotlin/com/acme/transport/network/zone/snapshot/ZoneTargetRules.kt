package com.acme.transport.network.zone.snapshot

/** A rule a zone target must pass. Check returns its problems, or none. */
interface ZoneTargetRule {
    fun check(item: ZoneTarget): MutableList<String>
}

data class ZoneTargetCeilingRangeRule(var min: Double, var max: Double) : ZoneTargetRule {
    override fun check(item: ZoneTarget): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class ZoneTargetOwnerIdLengthRule(var maxLength: Int) : ZoneTargetRule {
    override fun check(item: ZoneTarget): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneTargetActiveGuardRule(var limit: Int) : ZoneTargetRule {
    override fun check(item: ZoneTarget): MutableList<String> {
        if (item.active && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone target. */
data class ZoneTargetValidator(var rules: MutableList<ZoneTargetRule>) {
    fun validate(item: ZoneTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneTargetValidator(): ZoneTargetValidator {
    return ZoneTargetValidator(
        mutableListOf<ZoneTargetRule>(
            ZoneTargetCeilingRangeRule(5.2, 4879.1),
            ZoneTargetOwnerIdLengthRule(120),
            ZoneTargetActiveGuardRule(32),
        ),
    )
}
