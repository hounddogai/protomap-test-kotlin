package com.acme.transport.safety.cameraevent.budget

/** A rule a camera event milestone must pass. Check returns its problems, or none. */
interface CameraEventMilestoneRule {
    fun check(item: CameraEventMilestone): MutableList<String>
}

data class CameraEventMilestoneTitleLengthRule(var maxLength: Int) : CameraEventMilestoneRule {
    override fun check(item: CameraEventMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventMilestoneRequiredDockRule(var message: String) : CameraEventMilestoneRule {
    override fun check(item: CameraEventMilestone): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventMilestoneRequiredCodeRule(var message: String) : CameraEventMilestoneRule {
    override fun check(item: CameraEventMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event milestone. */
data class CameraEventMilestoneValidator(var rules: MutableList<CameraEventMilestoneRule>) {
    fun validate(item: CameraEventMilestone): MutableList<String> {
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

    fun requireValid(item: CameraEventMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventMilestoneValidator(): CameraEventMilestoneValidator {
    return CameraEventMilestoneValidator(
        mutableListOf<CameraEventMilestoneRule>(
            CameraEventMilestoneTitleLengthRule(1000),
            CameraEventMilestoneRequiredDockRule("camera event milestone needs a value"),
            CameraEventMilestoneRequiredCodeRule("camera event milestone needs this field"),
        ),
    )
}
