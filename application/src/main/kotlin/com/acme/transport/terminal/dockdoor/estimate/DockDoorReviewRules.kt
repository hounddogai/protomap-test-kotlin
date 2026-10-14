package com.acme.transport.terminal.dockdoor.estimate

/** A rule a dock door review must pass. Check returns its problems, or none. */
interface DockDoorReviewRule {
    fun check(item: DockDoorReview): MutableList<String>
}

data class DockDoorReviewRegionLengthRule(var maxLength: Int) : DockDoorReviewRule {
    override fun check(item: DockDoorReview): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorReviewCodeLengthRule(var maxLength: Int) : DockDoorReviewRule {
    override fun check(item: DockDoorReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorReviewCodePrefixRule(var prefix: String) : DockDoorReviewRule {
    override fun check(item: DockDoorReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorReviewRequiredCodeRule(var message: String) : DockDoorReviewRule {
    override fun check(item: DockDoorReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door review. */
data class DockDoorReviewValidator(var rules: MutableList<DockDoorReviewRule>) {
    fun validate(item: DockDoorReview): MutableList<String> {
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

    fun requireValid(item: DockDoorReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorReviewValidator(): DockDoorReviewValidator {
    return DockDoorReviewValidator(
        mutableListOf<DockDoorReviewRule>(
            DockDoorReviewRegionLengthRule(80),
            DockDoorReviewCodeLengthRule(16),
            DockDoorReviewCodePrefixRule("Q"),
            DockDoorReviewRequiredCodeRule("dock door review needs an entry"),
        ),
    )
}
