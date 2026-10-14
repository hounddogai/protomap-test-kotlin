package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a violation snapshot must pass. Check returns its problems, or none. */
interface ViolationSnapshotRule {
    fun check(item: ViolationSnapshot): MutableList<String>
}

data class ViolationSnapshotAllowedNotesRule(var allowed: MutableList<String>) : ViolationSnapshotRule {
    override fun check(item: ViolationSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ViolationSnapshotGraceDaysRangeRule(var min: Int, var max: Int) : ViolationSnapshotRule {
    override fun check(item: ViolationSnapshot): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationSnapshotRequiredNameRule(var message: String) : ViolationSnapshotRule {
    override fun check(item: ViolationSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationSnapshotNameLengthRule(var maxLength: Int) : ViolationSnapshotRule {
    override fun check(item: ViolationSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationSnapshotCodeLengthRule(var maxLength: Int) : ViolationSnapshotRule {
    override fun check(item: ViolationSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationSnapshotContactEmailLengthRule(var maxLength: Int) : ViolationSnapshotRule {
    override fun check(item: ViolationSnapshot): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation snapshot. */
data class ViolationSnapshotValidator(var rules: MutableList<ViolationSnapshotRule>) {
    fun validate(item: ViolationSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationSnapshotValidator(): ViolationSnapshotValidator {
    return ViolationSnapshotValidator(
        mutableListOf<ViolationSnapshotRule>(
            ViolationSnapshotAllowedNotesRule(mutableListOf<String>("primary", "default")),
            ViolationSnapshotGraceDaysRangeRule(64, 1000),
            ViolationSnapshotRequiredNameRule("violation snapshot needs an entry"),
            ViolationSnapshotNameLengthRule(255),
            ViolationSnapshotCodeLengthRule(86400),
            ViolationSnapshotContactEmailLengthRule(3600),
        ),
    )
}
