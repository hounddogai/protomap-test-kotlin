package com.acme.transport.freightpay.dispute.bundle

/** A rule a manual dispute bundle must pass. Check returns its problems, or none. */
interface ManualDisputeBundleRule {
    fun check(item: ManualDisputeBundle): MutableList<String>
}

data class ManualDisputeBundleNotesLengthRule(var maxLength: Int) : ManualDisputeBundleRule {
    override fun check(item: ManualDisputeBundle): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeBundleCodeLengthRule(var maxLength: Int) : ManualDisputeBundleRule {
    override fun check(item: ManualDisputeBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeBundleLabelLengthRule(var maxLength: Int) : ManualDisputeBundleRule {
    override fun check(item: ManualDisputeBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeBundleRegionLengthRule(var maxLength: Int) : ManualDisputeBundleRule {
    override fun check(item: ManualDisputeBundle): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDisputeBundleRequiredRegionRule(var message: String) : ManualDisputeBundleRule {
    override fun check(item: ManualDisputeBundle): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dispute bundle. */
data class ManualDisputeBundleValidator(var rules: MutableList<ManualDisputeBundleRule>) {
    fun validate(item: ManualDisputeBundle): MutableList<String> {
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

    fun requireValid(item: ManualDisputeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDisputeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDisputeBundleValidator(): ManualDisputeBundleValidator {
    return ManualDisputeBundleValidator(
        mutableListOf<ManualDisputeBundleRule>(
            ManualDisputeBundleNotesLengthRule(255),
            ManualDisputeBundleCodeLengthRule(86400),
            ManualDisputeBundleLabelLengthRule(120),
            ManualDisputeBundleRegionLengthRule(80),
            ManualDisputeBundleRequiredRegionRule("manual dispute bundle needs a value"),
        ),
    )
}
