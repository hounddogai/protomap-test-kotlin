package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.text.normalize

/** A rule a dock door target must pass. Check returns its problems, or none. */
interface DockDoorTargetRule {
    fun check(item: DockDoorTarget): MutableList<String>
}

data class DockDoorTargetVisibleGuardRule(var limit: Int) : DockDoorTargetRule {
    override fun check(item: DockDoorTarget): MutableList<String> {
        if (item.visible && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTargetDockLengthRule(var maxLength: Int) : DockDoorTargetRule {
    override fun check(item: DockDoorTarget): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTargetRequiredCodeRule(var message: String) : DockDoorTargetRule {
    override fun check(item: DockDoorTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTargetCodeLengthRule(var maxLength: Int) : DockDoorTargetRule {
    override fun check(item: DockDoorTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTargetRequiredDockRule(var message: String) : DockDoorTargetRule {
    override fun check(item: DockDoorTarget): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorTargetAllowedDockRule(var allowed: MutableList<String>) : DockDoorTargetRule {
    override fun check(item: DockDoorTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door target. */
data class DockDoorTargetValidator(var rules: MutableList<DockDoorTargetRule>) {
    fun validate(item: DockDoorTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorTargetValidator(): DockDoorTargetValidator {
    return DockDoorTargetValidator(
        mutableListOf<DockDoorTargetRule>(
            DockDoorTargetVisibleGuardRule(255),
            DockDoorTargetDockLengthRule(3600),
            DockDoorTargetRequiredCodeRule("dock door target needs a value"),
            DockDoorTargetCodeLengthRule(1000),
            DockDoorTargetRequiredDockRule("dock door target needs this field"),
            DockDoorTargetAllowedDockRule(mutableListOf<String>("rolling")),
        ),
    )
}
