package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.text.normalize

/** A rule a dock door manifest must pass. Check returns its problems, or none. */
interface DockDoorManifestRule {
    fun check(item: DockDoorManifest): MutableList<String>
}

data class DockDoorManifestApprovedGuardRule(var limit: Int) : DockDoorManifestRule {
    override fun check(item: DockDoorManifest): MutableList<String> {
        if (item.approved && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorManifestBudgetRangeRule(var min: Double, var max: Double) : DockDoorManifestRule {
    override fun check(item: DockDoorManifest): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class DockDoorManifestAllowedLabelRule(var allowed: MutableList<String>) : DockDoorManifestRule {
    override fun check(item: DockDoorManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorManifestRequiredLabelRule(var message: String) : DockDoorManifestRule {
    override fun check(item: DockDoorManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorManifestCodePrefixRule(var prefix: String) : DockDoorManifestRule {
    override fun check(item: DockDoorManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorManifestRequiredCodeRule(var message: String) : DockDoorManifestRule {
    override fun check(item: DockDoorManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door manifest. */
data class DockDoorManifestValidator(var rules: MutableList<DockDoorManifestRule>) {
    fun validate(item: DockDoorManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorManifestValidator(): DockDoorManifestValidator {
    return DockDoorManifestValidator(
        mutableListOf<DockDoorManifestRule>(
            DockDoorManifestApprovedGuardRule(80),
            DockDoorManifestBudgetRangeRule(7.0, 4827.7),
            DockDoorManifestAllowedLabelRule(mutableListOf<String>()),
            DockDoorManifestRequiredLabelRule("dock door manifest needs a value"),
            DockDoorManifestCodePrefixRule("X"),
            DockDoorManifestRequiredCodeRule("dock door manifest needs an entry"),
        ),
    )
}
