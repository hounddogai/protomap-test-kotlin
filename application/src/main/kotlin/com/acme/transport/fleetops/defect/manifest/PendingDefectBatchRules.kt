package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.text.normalize

/** A rule a pending defect batch must pass. Check returns its problems, or none. */
interface PendingDefectBatchRule {
    fun check(item: PendingDefectBatch): MutableList<String>
}

data class PendingDefectBatchTitleLengthRule(var maxLength: Int) : PendingDefectBatchRule {
    override fun check(item: PendingDefectBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectBatchActiveGuardRule(var limit: Double) : PendingDefectBatchRule {
    override fun check(item: PendingDefectBatch): MutableList<String> {
        if (item.active && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectBatchRequiredLabelRule(var message: String) : PendingDefectBatchRule {
    override fun check(item: PendingDefectBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectBatchCodeLengthRule(var maxLength: Int) : PendingDefectBatchRule {
    override fun check(item: PendingDefectBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectBatchAllowedOwnerIdRule(var allowed: MutableList<String>) : PendingDefectBatchRule {
    override fun check(item: PendingDefectBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending defect batch. */
data class PendingDefectBatchValidator(var rules: MutableList<PendingDefectBatchRule>) {
    fun validate(item: PendingDefectBatch): MutableList<String> {
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

    fun requireValid(item: PendingDefectBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDefectBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDefectBatchValidator(): PendingDefectBatchValidator {
    return PendingDefectBatchValidator(
        mutableListOf<PendingDefectBatchRule>(
            PendingDefectBatchTitleLengthRule(1000),
            PendingDefectBatchActiveGuardRule(6.1),
            PendingDefectBatchRequiredLabelRule("pending defect batch needs a value"),
            PendingDefectBatchCodeLengthRule(120),
            PendingDefectBatchAllowedOwnerIdRule(mutableListOf<String>()),
        ),
    )
}
