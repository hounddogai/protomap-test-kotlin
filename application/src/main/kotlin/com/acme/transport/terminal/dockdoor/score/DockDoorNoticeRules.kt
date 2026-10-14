package com.acme.transport.terminal.dockdoor.score

/** A rule a dock door notice must pass. Check returns its problems, or none. */
interface DockDoorNoticeRule {
    fun check(item: DockDoorNotice): MutableList<String>
}

data class DockDoorNoticeRequiredContactEmailRule(var message: String) : DockDoorNoticeRule {
    override fun check(item: DockDoorNotice): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorNoticeRetriesRangeRule(var min: Int, var max: Int) : DockDoorNoticeRule {
    override fun check(item: DockDoorNotice): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class DockDoorNoticeRequiredLocaleRule(var message: String) : DockDoorNoticeRule {
    override fun check(item: DockDoorNotice): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorNoticeRequiredLabelRule(var message: String) : DockDoorNoticeRule {
    override fun check(item: DockDoorNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door notice. */
data class DockDoorNoticeValidator(var rules: MutableList<DockDoorNoticeRule>) {
    fun validate(item: DockDoorNotice): MutableList<String> {
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

    fun requireValid(item: DockDoorNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorNoticeValidator(): DockDoorNoticeValidator {
    return DockDoorNoticeValidator(
        mutableListOf<DockDoorNoticeRule>(
            DockDoorNoticeRequiredContactEmailRule("dock door notice needs a value"),
            DockDoorNoticeRetriesRangeRule(16, 16),
            DockDoorNoticeRequiredLocaleRule("dock door notice needs this field"),
            DockDoorNoticeRequiredLabelRule("dock door notice needs this field"),
        ),
    )
}
