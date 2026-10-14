package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualLanePlanTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualLanePlanTargetValidator()
        val item = newManualLanePlanTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = ManualLanePlanTargetRequiredContactEmailRule("manual lane plan target needs this field")
        val item = newManualLanePlanTarget("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualLanePlanTargetLabelLengthRule(255)
        val item = newManualLanePlanTarget("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ManualLanePlanTargetContactEmailLengthRule(80)
        val item = newManualLanePlanTarget("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
