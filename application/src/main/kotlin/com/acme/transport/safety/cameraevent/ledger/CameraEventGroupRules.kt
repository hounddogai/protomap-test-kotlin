package com.acme.transport.safety.cameraevent.ledger

/** A rule a camera event group must pass. Check returns its problems, or none. */
interface CameraEventGroupRule {
    fun check(item: CameraEventGroup): MutableList<String>
}

data class CameraEventGroupRequiredCodeRule(var message: String) : CameraEventGroupRule {
    override fun check(item: CameraEventGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventGroupRequiredDockRule(var message: String) : CameraEventGroupRule {
    override fun check(item: CameraEventGroup): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventGroupRequiredTrailerIdRule(var message: String) : CameraEventGroupRule {
    override fun check(item: CameraEventGroup): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventGroupNameLengthRule(var maxLength: Int) : CameraEventGroupRule {
    override fun check(item: CameraEventGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventGroupDockLengthRule(var maxLength: Int) : CameraEventGroupRule {
    override fun check(item: CameraEventGroup): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event group. */
data class CameraEventGroupValidator(var rules: MutableList<CameraEventGroupRule>) {
    fun validate(item: CameraEventGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CameraEventGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventGroupValidator(): CameraEventGroupValidator {
    return CameraEventGroupValidator(
        mutableListOf<CameraEventGroupRule>(
            CameraEventGroupRequiredCodeRule("camera event group needs an entry"),
            CameraEventGroupRequiredDockRule("camera event group needs a value"),
            CameraEventGroupRequiredTrailerIdRule("camera event group needs this field"),
            CameraEventGroupNameLengthRule(255),
            CameraEventGroupDockLengthRule(16),
        ),
    )
}
