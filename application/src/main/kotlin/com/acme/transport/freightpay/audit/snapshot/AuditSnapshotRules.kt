package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a audit snapshot must pass. Check returns its problems, or none. */
interface AuditSnapshotRule {
    fun check(item: AuditSnapshot): MutableList<String>
}

data class AuditSnapshotShiftCodeLengthRule(var maxLength: Int) : AuditSnapshotRule {
    override fun check(item: AuditSnapshot): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditSnapshotAllowedSegmentRule(var allowed: MutableList<String>) : AuditSnapshotRule {
    override fun check(item: AuditSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AuditSnapshotMaximumRangeRule(var min: Double, var max: Double) : AuditSnapshotRule {
    override fun check(item: AuditSnapshot): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditSnapshotRequiredNameRule(var message: String) : AuditSnapshotRule {
    override fun check(item: AuditSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit snapshot. */
data class AuditSnapshotValidator(var rules: MutableList<AuditSnapshotRule>) {
    fun validate(item: AuditSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditSnapshotValidator(): AuditSnapshotValidator {
    return AuditSnapshotValidator(
        mutableListOf<AuditSnapshotRule>(
            AuditSnapshotShiftCodeLengthRule(86400),
            AuditSnapshotAllowedSegmentRule(mutableListOf<String>("manual", "pending", "fallback")),
            AuditSnapshotMaximumRangeRule(6.3, 4053.3),
            AuditSnapshotRequiredNameRule("audit snapshot needs a value"),
        ),
    )
}
