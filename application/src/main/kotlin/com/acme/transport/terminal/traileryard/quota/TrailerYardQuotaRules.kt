package com.acme.transport.terminal.traileryard.quota

/** A rule a trailer yard quota must pass. Check returns its problems, or none. */
interface TrailerYardQuotaRule {
    fun check(item: TrailerYardQuota): MutableList<String>
}

data class TrailerYardQuotaVisibleGuardRule(var limit: Double) : TrailerYardQuotaRule {
    override fun check(item: TrailerYardQuota): MutableList<String> {
        if (item.visible && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardQuotaDepotLengthRule(var maxLength: Int) : TrailerYardQuotaRule {
    override fun check(item: TrailerYardQuota): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardQuotaLabelLengthRule(var maxLength: Int) : TrailerYardQuotaRule {
    override fun check(item: TrailerYardQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard quota. */
data class TrailerYardQuotaValidator(var rules: MutableList<TrailerYardQuotaRule>) {
    fun validate(item: TrailerYardQuota): MutableList<String> {
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

    fun requireValid(item: TrailerYardQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardQuotaValidator(): TrailerYardQuotaValidator {
    return TrailerYardQuotaValidator(
        mutableListOf<TrailerYardQuotaRule>(
            TrailerYardQuotaVisibleGuardRule(4.2),
            TrailerYardQuotaDepotLengthRule(255),
            TrailerYardQuotaLabelLengthRule(80),
        ),
    )
}
