package com.acme.transport.crewing.swap.exception

/** A rule a rolling swap segment must pass. Check returns its problems, or none. */
interface RollingSwapSegmentRule {
    fun check(item: RollingSwapSegment): MutableList<String>
}

data class RollingSwapSegmentDescriptionLengthRule(var maxLength: Int) : RollingSwapSegmentRule {
    override fun check(item: RollingSwapSegment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapSegmentPinnedGuardRule(var limit: Double) : RollingSwapSegmentRule {
    override fun check(item: RollingSwapSegment): MutableList<String> {
        if (item.pinned && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapSegmentRequiredDriverIdRule(var message: String) : RollingSwapSegmentRule {
    override fun check(item: RollingSwapSegment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapSegmentRequiredNameRule(var message: String) : RollingSwapSegmentRule {
    override fun check(item: RollingSwapSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapSegmentCodeLengthRule(var maxLength: Int) : RollingSwapSegmentRule {
    override fun check(item: RollingSwapSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapSegmentRequiredDescriptionRule(var message: String) : RollingSwapSegmentRule {
    override fun check(item: RollingSwapSegment): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling swap segment. */
data class RollingSwapSegmentValidator(var rules: MutableList<RollingSwapSegmentRule>) {
    fun validate(item: RollingSwapSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingSwapSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSwapSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSwapSegmentValidator(): RollingSwapSegmentValidator {
    return RollingSwapSegmentValidator(
        mutableListOf<RollingSwapSegmentRule>(
            RollingSwapSegmentDescriptionLengthRule(80),
            RollingSwapSegmentPinnedGuardRule(6.9),
            RollingSwapSegmentRequiredDriverIdRule("rolling swap segment needs an entry"),
            RollingSwapSegmentRequiredNameRule("rolling swap segment needs an entry"),
            RollingSwapSegmentCodeLengthRule(64),
            RollingSwapSegmentRequiredDescriptionRule("rolling swap segment needs an entry"),
        ),
    )
}
