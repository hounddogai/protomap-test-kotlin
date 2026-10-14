package com.acme.transport.lastmile.locker.target

/** A rule a locker review must pass. Check returns its problems, or none. */
interface LockerReviewRule {
    fun check(item: LockerReview): MutableList<String>
}

data class LockerReviewRequiredSegmentRule(var message: String) : LockerReviewRule {
    override fun check(item: LockerReview): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerReviewCodePrefixRule(var prefix: String) : LockerReviewRule {
    override fun check(item: LockerReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerReviewRequiredCodeRule(var message: String) : LockerReviewRule {
    override fun check(item: LockerReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerReviewRequiredDescriptionRule(var message: String) : LockerReviewRule {
    override fun check(item: LockerReview): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker review. */
data class LockerReviewValidator(var rules: MutableList<LockerReviewRule>) {
    fun validate(item: LockerReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerReviewValidator(): LockerReviewValidator {
    return LockerReviewValidator(
        mutableListOf<LockerReviewRule>(
            LockerReviewRequiredSegmentRule("locker review needs an entry"),
            LockerReviewCodePrefixRule("X"),
            LockerReviewRequiredCodeRule("locker review needs an entry"),
            LockerReviewRequiredDescriptionRule("locker review needs this field"),
        ),
    )
}
