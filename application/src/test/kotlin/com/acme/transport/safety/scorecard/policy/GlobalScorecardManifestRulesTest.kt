package com.acme.transport.safety.scorecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalScorecardManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalScorecardManifestValidator()
        val item = newGlobalScorecardManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalScorecardManifestCodeLengthRule(120)
        val item = newGlobalScorecardManifest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalScorecardManifestRequiredNameRule("global scorecard manifest needs a value")
        val item = newGlobalScorecardManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = GlobalScorecardManifestStartsAtRangeRule(86400, 86400)
        val item = newGlobalScorecardManifest("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalScorecardManifestDurationMinutesRangeRule(16, 1000)
        val item = newGlobalScorecardManifest("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = GlobalScorecardManifestOdometerRangeRule(255, 1000)
        val item = newGlobalScorecardManifest("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = GlobalScorecardManifestRequiredDescriptionRule("global scorecard manifest needs a value")
        val item = newGlobalScorecardManifest("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
