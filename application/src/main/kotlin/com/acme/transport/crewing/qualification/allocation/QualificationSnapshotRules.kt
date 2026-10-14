package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.normalize

/** A rule a qualification snapshot must pass. Check returns its problems, or none. */
interface QualificationSnapshotRule {
    fun check(item: QualificationSnapshot): MutableList<String>
}

data class QualificationSnapshotNameLengthRule(var maxLength: Int) : QualificationSnapshotRule {
    override fun check(item: QualificationSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSnapshotRequiredNameRule(var message: String) : QualificationSnapshotRule {
    override fun check(item: QualificationSnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class QualificationSnapshotAllowedNameRule(var allowed: MutableList<String>) : QualificationSnapshotRule {
    override fun check(item: QualificationSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSnapshotCostRangeRule(var min: Double, var max: Double) : QualificationSnapshotRule {
    override fun check(item: QualificationSnapshot): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationSnapshotCreatedAtRangeRule(var min: Int, var max: Int) : QualificationSnapshotRule {
    override fun check(item: QualificationSnapshot): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification snapshot. */
data class QualificationSnapshotValidator(var rules: MutableList<QualificationSnapshotRule>) {
    fun validate(item: QualificationSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationSnapshotValidator(): QualificationSnapshotValidator {
    return QualificationSnapshotValidator(
        mutableListOf<QualificationSnapshotRule>(
            QualificationSnapshotNameLengthRule(80),
            QualificationSnapshotRequiredNameRule("qualification snapshot needs an entry"),
            QualificationSnapshotAllowedNameRule(mutableListOf<String>("manual")),
            QualificationSnapshotCostRangeRule(9.9, 1838.6),
            QualificationSnapshotCreatedAtRangeRule(16, 3600),
        ),
    )
}
