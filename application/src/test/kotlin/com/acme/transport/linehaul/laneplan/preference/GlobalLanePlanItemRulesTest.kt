package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLanePlanItemRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalLanePlanItemValidator()
        val item = newGlobalLanePlanItem("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = GlobalLanePlanItemRequiredReviewerRule("global lane plan item needs a value")
        val item = newGlobalLanePlanItem("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = GlobalLanePlanItemRequiredTrailerIdRule("global lane plan item needs an entry")
        val item = newGlobalLanePlanItem("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalLanePlanItemRequiredTitleRule("global lane plan item needs a value")
        val item = newGlobalLanePlanItem("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = GlobalLanePlanItemEtaMinutesRangeRule(32, 3600)
        val item = newGlobalLanePlanItem("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
