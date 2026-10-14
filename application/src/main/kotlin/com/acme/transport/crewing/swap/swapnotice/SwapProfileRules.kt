package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.text.normalize

/** A rule a swap profile must pass. Check returns its problems, or none. */
interface SwapProfileRule {
    fun check(item: SwapProfile): MutableList<String>
}

data class SwapProfilePinnedGuardRule(var limit: Int) : SwapProfileRule {
    override fun check(item: SwapProfile): MutableList<String> {
        if (item.pinned && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class SwapProfileCodeLengthRule(var maxLength: Int) : SwapProfileRule {
    override fun check(item: SwapProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapProfileNameLengthRule(var maxLength: Int) : SwapProfileRule {
    override fun check(item: SwapProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapProfileAllowedNameRule(var allowed: MutableList<String>) : SwapProfileRule {
    override fun check(item: SwapProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap profile. */
data class SwapProfileValidator(var rules: MutableList<SwapProfileRule>) {
    fun validate(item: SwapProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapProfileValidator(): SwapProfileValidator {
    return SwapProfileValidator(
        mutableListOf<SwapProfileRule>(
            SwapProfilePinnedGuardRule(3600),
            SwapProfileCodeLengthRule(3600),
            SwapProfileNameLengthRule(64),
            SwapProfileAllowedNameRule(mutableListOf<String>("strict")),
        ),
    )
}
