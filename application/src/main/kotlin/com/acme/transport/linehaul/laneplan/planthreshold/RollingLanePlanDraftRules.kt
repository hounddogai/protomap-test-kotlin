package com.acme.transport.linehaul.laneplan.planthreshold

/** A rule a rolling lane plan draft must pass. Check returns its problems, or none. */
interface RollingLanePlanDraftRule {
    fun check(item: RollingLanePlanDraft): MutableList<String>
}

data class RollingLanePlanDraftBatchSizeRangeRule(var min: Int, var max: Int) : RollingLanePlanDraftRule {
    override fun check(item: RollingLanePlanDraft): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanDraftRequiredNameRule(var message: String) : RollingLanePlanDraftRule {
    override fun check(item: RollingLanePlanDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanDraftDockLengthRule(var maxLength: Int) : RollingLanePlanDraftRule {
    override fun check(item: RollingLanePlanDraft): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingLanePlanDraftRequiredDepotRule(var message: String) : RollingLanePlanDraftRule {
    override fun check(item: RollingLanePlanDraft): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling lane plan draft. */
data class RollingLanePlanDraftValidator(var rules: MutableList<RollingLanePlanDraftRule>) {
    fun validate(item: RollingLanePlanDraft): MutableList<String> {
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

    fun requireValid(item: RollingLanePlanDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingLanePlanDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingLanePlanDraftValidator(): RollingLanePlanDraftValidator {
    return RollingLanePlanDraftValidator(
        mutableListOf<RollingLanePlanDraftRule>(
            RollingLanePlanDraftBatchSizeRangeRule(64, 255),
            RollingLanePlanDraftRequiredNameRule("rolling lane plan draft needs a value"),
            RollingLanePlanDraftDockLengthRule(16),
            RollingLanePlanDraftRequiredDepotRule("rolling lane plan draft needs an entry"),
        ),
    )
}
