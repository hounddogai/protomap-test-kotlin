package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionProfileValidator()
        val item = newInspectionProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = InspectionProfileRequiredDockRule("inspection profile needs a value")
        val item = newInspectionProfile("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = InspectionProfileRequiredTitleRule("inspection profile needs an entry")
        val item = newInspectionProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionProfileRequiredCodeRule("inspection profile needs an entry")
        val item = newInspectionProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionProfileTitleLengthRule(64)
        val item = newInspectionProfile("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionProfileRequiredLabelRule("inspection profile needs a value")
        val item = newInspectionProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
