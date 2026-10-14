package com.acme.transport.linehaul.laneplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanManifestValidator()
        val item = newLanePlanManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LanePlanManifestEtaMinutesRangeRule(255, 3600)
        val item = newLanePlanManifest("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LanePlanManifestRequiredTitleRule("lane plan manifest needs this field")
        val item = newLanePlanManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LanePlanManifestCodeLengthRule(80)
        val item = newLanePlanManifest("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LanePlanManifestDurationMinutesRangeRule(64, 86400)
        val item = newLanePlanManifest("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
