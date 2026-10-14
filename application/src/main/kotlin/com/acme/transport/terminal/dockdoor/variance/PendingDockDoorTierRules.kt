package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.text.normalize

/** A rule a pending dock door tier must pass. Check returns its problems, or none. */
interface PendingDockDoorTierRule {
    fun check(item: PendingDockDoorTier): MutableList<String>
}

data class PendingDockDoorTierRegionLengthRule(var maxLength: Int) : PendingDockDoorTierRule {
    override fun check(item: PendingDockDoorTier): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorTierAllowedCodeRule(var allowed: MutableList<String>) : PendingDockDoorTierRule {
    override fun check(item: PendingDockDoorTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorTierRequiredCodeRule(var message: String) : PendingDockDoorTierRule {
    override fun check(item: PendingDockDoorTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorTierTimezoneLengthRule(var maxLength: Int) : PendingDockDoorTierRule {
    override fun check(item: PendingDockDoorTier): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorTierCodeLengthRule(var maxLength: Int) : PendingDockDoorTierRule {
    override fun check(item: PendingDockDoorTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dock door tier. */
data class PendingDockDoorTierValidator(var rules: MutableList<PendingDockDoorTierRule>) {
    fun validate(item: PendingDockDoorTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDockDoorTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDockDoorTier>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDockDoorTierValidator(): PendingDockDoorTierValidator {
    return PendingDockDoorTierValidator(
        mutableListOf<PendingDockDoorTierRule>(
            PendingDockDoorTierRegionLengthRule(80),
            PendingDockDoorTierAllowedCodeRule(mutableListOf<String>()),
            PendingDockDoorTierRequiredCodeRule("pending dock door tier needs an entry"),
            PendingDockDoorTierTimezoneLengthRule(1000),
            PendingDockDoorTierCodeLengthRule(3600),
        ),
    )
}
