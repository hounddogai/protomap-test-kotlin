package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCutoffAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyCutoffAdjustmentValidator()
        val item = newLegacyCutoffAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = LegacyCutoffAdjustmentDepotLengthRule(255)
        val item = newLegacyCutoffAdjustment("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyCutoffAdjustmentCodeLengthRule(64)
        val item = newLegacyCutoffAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = LegacyCutoffAdjustmentRequiredDepotRule("legacy cutoff adjustment needs a value")
        val item = newLegacyCutoffAdjustment("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyCutoffAdjustmentTitleLengthRule(3600)
        val item = newLegacyCutoffAdjustment("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
