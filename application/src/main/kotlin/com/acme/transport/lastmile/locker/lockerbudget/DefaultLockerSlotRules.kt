package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.text.normalize

/** A rule a default locker slot must pass. Check returns its problems, or none. */
interface DefaultLockerSlotRule {
    fun check(item: DefaultLockerSlot): MutableList<String>
}

data class DefaultLockerSlotAllowedCodeRule(var allowed: MutableList<String>) : DefaultLockerSlotRule {
    override fun check(item: DefaultLockerSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerSlotRequiredSegmentRule(var message: String) : DefaultLockerSlotRule {
    override fun check(item: DefaultLockerSlot): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultLockerSlotTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DefaultLockerSlotRule {
    override fun check(item: DefaultLockerSlot): MutableList<String> {
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

data class DefaultLockerSlotNameLengthRule(var maxLength: Int) : DefaultLockerSlotRule {
    override fun check(item: DefaultLockerSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default locker slot. */
data class DefaultLockerSlotValidator(var rules: MutableList<DefaultLockerSlotRule>) {
    fun validate(item: DefaultLockerSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultLockerSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultLockerSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultLockerSlotValidator(): DefaultLockerSlotValidator {
    return DefaultLockerSlotValidator(
        mutableListOf<DefaultLockerSlotRule>(
            DefaultLockerSlotAllowedCodeRule(mutableListOf<String>("fallback", "default", "batched")),
            DefaultLockerSlotRequiredSegmentRule("default locker slot needs an entry"),
            DefaultLockerSlotTagsLimitRule(80, mutableListOf<String>()),
            DefaultLockerSlotNameLengthRule(32),
        ),
    )
}
