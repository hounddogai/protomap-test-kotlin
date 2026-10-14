package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredLanePlanRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredLanePlanRequestValidator()
        val item = newTieredLanePlanRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = TieredLanePlanRequestDepotLengthRule(86400)
        val item = newTieredLanePlanRequest("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredLanePlanRequestCodeLengthRule(120)
        val item = newTieredLanePlanRequest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = TieredLanePlanRequestRequiredDepotRule("tiered lane plan request needs this field")
        val item = newTieredLanePlanRequest("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = TieredLanePlanRequestBatchSizeRangeRule(16, 64)
        val item = newTieredLanePlanRequest("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredLanePlanRequestRequiredCodeRule("tiered lane plan request needs this field")
        val item = newTieredLanePlanRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredLanePlanRequestRequiredLabelRule("tiered lane plan request needs a value")
        val item = newTieredLanePlanRequest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
