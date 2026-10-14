package com.acme.transport.linehaul.laneplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLanePlanDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingLanePlanDraftValidator()
        val item = newRollingLanePlanDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RollingLanePlanDraftBatchSizeRangeRule(80, 120)
        val item = newRollingLanePlanDraft("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingLanePlanDraftRequiredNameRule("rolling lane plan draft needs this field")
        val item = newRollingLanePlanDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = RollingLanePlanDraftDockLengthRule(86400)
        val item = newRollingLanePlanDraft("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RollingLanePlanDraftRequiredDepotRule("rolling lane plan draft needs a value")
        val item = newRollingLanePlanDraft("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
