package com.acme.transport.lastmile.drop.rollup

/** A rule a regional drop target must pass. Check returns its problems, or none. */
interface RegionalDropTargetRule {
    fun check(item: RegionalDropTarget): MutableList<String>
}

data class RegionalDropTargetVisibleGuardRule(var limit: Int) : RegionalDropTargetRule {
    override fun check(item: RegionalDropTarget): MutableList<String> {
        if (item.visible && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropTargetRequiredTitleRule(var message: String) : RegionalDropTargetRule {
    override fun check(item: RegionalDropTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropTargetCodePrefixRule(var prefix: String) : RegionalDropTargetRule {
    override fun check(item: RegionalDropTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDropTargetRequiredTrailerIdRule(var message: String) : RegionalDropTargetRule {
    override fun check(item: RegionalDropTarget): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional drop target. */
data class RegionalDropTargetValidator(var rules: MutableList<RegionalDropTargetRule>) {
    fun validate(item: RegionalDropTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDropTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDropTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDropTargetValidator(): RegionalDropTargetValidator {
    return RegionalDropTargetValidator(
        mutableListOf<RegionalDropTargetRule>(
            RegionalDropTargetVisibleGuardRule(32),
            RegionalDropTargetRequiredTitleRule("regional drop target needs this field"),
            RegionalDropTargetCodePrefixRule("OPS"),
            RegionalDropTargetRequiredTrailerIdRule("regional drop target needs a value"),
        ),
    )
}
