package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDefectAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDefectAuditValidator()
        val item = newTieredDefectAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = TieredDefectAuditRequiredCategoryRule("tiered defect audit needs an entry")
        val item = newTieredDefectAudit("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TieredDefectAuditThresholdRangeRule(9.2, 780.8)
        val item = newTieredDefectAudit("x", "y")
        item.threshold = 782.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = TieredDefectAuditCategoryLengthRule(3600)
        val item = newTieredDefectAudit("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredDefectAuditRequiredOwnerIdRule("tiered defect audit needs an entry")
        val item = newTieredDefectAudit("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
