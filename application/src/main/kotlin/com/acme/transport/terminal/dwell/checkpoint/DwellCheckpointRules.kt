package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a dwell checkpoint must pass. Check returns its problems, or none. */
interface DwellCheckpointRule {
    fun check(item: DwellCheckpoint): MutableList<String>
}

data class DwellCheckpointAllowedContactEmailRule(var allowed: MutableList<String>) : DwellCheckpointRule {
    override fun check(item: DwellCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DwellCheckpointRequiredCodeRule(var message: String) : DwellCheckpointRule {
    override fun check(item: DwellCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellCheckpointTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DwellCheckpointRule {
    override fun check(item: DwellCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class DwellCheckpointRequiredShiftCodeRule(var message: String) : DwellCheckpointRule {
    override fun check(item: DwellCheckpoint): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellCheckpointRequiredLabelRule(var message: String) : DwellCheckpointRule {
    override fun check(item: DwellCheckpoint): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellCheckpointPinnedGuardRule(var limit: Double) : DwellCheckpointRule {
    override fun check(item: DwellCheckpoint): MutableList<String> {
        if (item.pinned && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell checkpoint. */
data class DwellCheckpointValidator(var rules: MutableList<DwellCheckpointRule>) {
    fun validate(item: DwellCheckpoint): MutableList<String> {
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

    fun requireValid(item: DwellCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellCheckpointValidator(): DwellCheckpointValidator {
    return DwellCheckpointValidator(
        mutableListOf<DwellCheckpointRule>(
            DwellCheckpointAllowedContactEmailRule(mutableListOf<String>("regional", "batched")),
            DwellCheckpointRequiredCodeRule("dwell checkpoint needs an entry"),
            DwellCheckpointTagsLimitRule(80, mutableListOf<String>("pending", "seasonal", "global")),
            DwellCheckpointRequiredShiftCodeRule("dwell checkpoint needs a value"),
            DwellCheckpointRequiredLabelRule("dwell checkpoint needs this field"),
            DwellCheckpointPinnedGuardRule(4.7),
        ),
    )
}
