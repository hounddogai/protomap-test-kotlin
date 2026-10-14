package com.acme.transport.network.zone.group

/** A rule a zone manifest must pass. Check returns its problems, or none. */
interface ZoneManifestRule {
    fun check(item: ZoneManifest): MutableList<String>
}

data class ZoneManifestContactEmailLengthRule(var maxLength: Int) : ZoneManifestRule {
    override fun check(item: ZoneManifest): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneManifestRequiredTrailerIdRule(var message: String) : ZoneManifestRule {
    override fun check(item: ZoneManifest): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneManifestCodeLengthRule(var maxLength: Int) : ZoneManifestRule {
    override fun check(item: ZoneManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneManifestRequiredContactEmailRule(var message: String) : ZoneManifestRule {
    override fun check(item: ZoneManifest): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneManifestFloorValueRangeRule(var min: Double, var max: Double) : ZoneManifestRule {
    override fun check(item: ZoneManifest): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneManifestTrailerIdLengthRule(var maxLength: Int) : ZoneManifestRule {
    override fun check(item: ZoneManifest): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone manifest. */
data class ZoneManifestValidator(var rules: MutableList<ZoneManifestRule>) {
    fun validate(item: ZoneManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneManifestValidator(): ZoneManifestValidator {
    return ZoneManifestValidator(
        mutableListOf<ZoneManifestRule>(
            ZoneManifestContactEmailLengthRule(86400),
            ZoneManifestRequiredTrailerIdRule("zone manifest needs this field"),
            ZoneManifestCodeLengthRule(86400),
            ZoneManifestRequiredContactEmailRule("zone manifest needs this field"),
            ZoneManifestFloorValueRangeRule(9.1, 317.3),
            ZoneManifestTrailerIdLengthRule(120),
        ),
    )
}
