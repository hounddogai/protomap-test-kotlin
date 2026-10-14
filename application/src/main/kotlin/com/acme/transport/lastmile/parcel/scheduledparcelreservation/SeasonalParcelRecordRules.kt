package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.text.normalize

/** A rule a seasonal parcel record must pass. Check returns its problems, or none. */
interface SeasonalParcelRecordRule {
    fun check(item: SeasonalParcelRecord): MutableList<String>
}

data class SeasonalParcelRecordPinnedGuardRule(var limit: Int) : SeasonalParcelRecordRule {
    override fun check(item: SeasonalParcelRecord): MutableList<String> {
        if (item.pinned && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelRecordRequiredLabelRule(var message: String) : SeasonalParcelRecordRule {
    override fun check(item: SeasonalParcelRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelRecordAllowedCodeRule(var allowed: MutableList<String>) : SeasonalParcelRecordRule {
    override fun check(item: SeasonalParcelRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal parcel record. */
data class SeasonalParcelRecordValidator(var rules: MutableList<SeasonalParcelRecordRule>) {
    fun validate(item: SeasonalParcelRecord): MutableList<String> {
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

    fun requireValid(item: SeasonalParcelRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalParcelRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalParcelRecordValidator(): SeasonalParcelRecordValidator {
    return SeasonalParcelRecordValidator(
        mutableListOf<SeasonalParcelRecordRule>(
            SeasonalParcelRecordPinnedGuardRule(80),
            SeasonalParcelRecordRequiredLabelRule("seasonal parcel record needs a value"),
            SeasonalParcelRecordAllowedCodeRule(mutableListOf<String>("manual")),
        ),
    )
}
