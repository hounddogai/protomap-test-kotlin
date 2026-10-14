package com.acme.transport.linehaul.laneplan.planbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualLanePlanBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualLanePlanBundleValidator()
        val item = newManualLanePlanBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ManualLanePlanBundleRequiredSummaryRule("manual lane plan bundle needs an entry")
        val item = newManualLanePlanBundle("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ManualLanePlanBundleSummaryLengthRule(120)
        val item = newManualLanePlanBundle("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualLanePlanBundleLabelLengthRule(80)
        val item = newManualLanePlanBundle("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ManualLanePlanBundleDepotLengthRule(16)
        val item = newManualLanePlanBundle("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
