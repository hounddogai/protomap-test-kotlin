package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.text.normalize

/** A rule a primary dwell snapshot must pass. Check returns its problems, or none. */
interface PrimaryDwellSnapshotRule {
    fun check(item: PrimaryDwellSnapshot): MutableList<String>
}

data class PrimaryDwellSnapshotAllowedCodeRule(var allowed: MutableList<String>) : PrimaryDwellSnapshotRule {
    override fun check(item: PrimaryDwellSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellSnapshotRequiredChannelRule(var message: String) : PrimaryDwellSnapshotRule {
    override fun check(item: PrimaryDwellSnapshot): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellSnapshotCodePrefixRule(var prefix: String) : PrimaryDwellSnapshotRule {
    override fun check(item: PrimaryDwellSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellSnapshotRequiredLabelRule(var message: String) : PrimaryDwellSnapshotRule {
    override fun check(item: PrimaryDwellSnapshot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellSnapshotRequiredDockRule(var message: String) : PrimaryDwellSnapshotRule {
    override fun check(item: PrimaryDwellSnapshot): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDwellSnapshotStopCountRangeRule(var min: Int, var max: Int) : PrimaryDwellSnapshotRule {
    override fun check(item: PrimaryDwellSnapshot): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary dwell snapshot. */
data class PrimaryDwellSnapshotValidator(var rules: MutableList<PrimaryDwellSnapshotRule>) {
    fun validate(item: PrimaryDwellSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDwellSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDwellSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDwellSnapshotValidator(): PrimaryDwellSnapshotValidator {
    return PrimaryDwellSnapshotValidator(
        mutableListOf<PrimaryDwellSnapshotRule>(
            PrimaryDwellSnapshotAllowedCodeRule(mutableListOf<String>("legacy", "scheduled")),
            PrimaryDwellSnapshotRequiredChannelRule("primary dwell snapshot needs an entry"),
            PrimaryDwellSnapshotCodePrefixRule("REF"),
            PrimaryDwellSnapshotRequiredLabelRule("primary dwell snapshot needs this field"),
            PrimaryDwellSnapshotRequiredDockRule("primary dwell snapshot needs this field"),
            PrimaryDwellSnapshotStopCountRangeRule(16, 1000),
        ),
    )
}
