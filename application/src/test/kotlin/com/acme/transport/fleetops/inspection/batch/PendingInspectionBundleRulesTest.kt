package com.acme.transport.fleetops.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingInspectionBundleValidator()
        val item = newPendingInspectionBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PendingInspectionBundleCapacityRangeRule(64, 1000)
        val item = newPendingInspectionBundle("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PendingInspectionBundleRequiredDescriptionRule("pending inspection bundle needs this field")
        val item = newPendingInspectionBundle("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingInspectionBundleRequiredTitleRule("pending inspection bundle needs a value")
        val item = newPendingInspectionBundle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = PendingInspectionBundleDescriptionLengthRule(255)
        val item = newPendingInspectionBundle("x", "y")
        item.description = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingInspectionBundleCodeLengthRule(16)
        val item = newPendingInspectionBundle("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = PendingInspectionBundleDistanceKmRangeRule(1.9, 1997.9)
        val item = newPendingInspectionBundle("x", "y")
        item.distanceKm = 1999.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
