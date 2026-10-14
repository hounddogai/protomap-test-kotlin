package com.acme.transport.terminal.dwell.digest

/** A rule a dwell digest must pass. Check returns its problems, or none. */
interface DwellDigestRule {
    fun check(item: DwellDigest): MutableList<String>
}

data class DwellDigestRequiredCodeRule(var message: String) : DwellDigestRule {
    override fun check(item: DwellDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellDigestRequiredLabelRule(var message: String) : DwellDigestRule {
    override fun check(item: DwellDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellDigestRequiredDockRule(var message: String) : DwellDigestRule {
    override fun check(item: DwellDigest): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellDigestCodeLengthRule(var maxLength: Int) : DwellDigestRule {
    override fun check(item: DwellDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellDigestLabelLengthRule(var maxLength: Int) : DwellDigestRule {
    override fun check(item: DwellDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell digest. */
data class DwellDigestValidator(var rules: MutableList<DwellDigestRule>) {
    fun validate(item: DwellDigest): MutableList<String> {
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

    fun requireValid(item: DwellDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellDigestValidator(): DwellDigestValidator {
    return DwellDigestValidator(
        mutableListOf<DwellDigestRule>(
            DwellDigestRequiredCodeRule("dwell digest needs a value"),
            DwellDigestRequiredLabelRule("dwell digest needs this field"),
            DwellDigestRequiredDockRule("dwell digest needs this field"),
            DwellDigestCodeLengthRule(64),
            DwellDigestLabelLengthRule(255),
        ),
    )
}
