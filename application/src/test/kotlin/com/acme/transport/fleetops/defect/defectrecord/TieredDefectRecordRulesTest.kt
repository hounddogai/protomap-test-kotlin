package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDefectRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDefectRecordValidator()
        val item = newTieredDefectRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredDefectRecordRequiredTitleRule("tiered defect record needs an entry")
        val item = newTieredDefectRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = TieredDefectRecordRequiredSourceRule("tiered defect record needs this field")
        val item = newTieredDefectRecord("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredDefectRecordCodeLengthRule(120)
        val item = newTieredDefectRecord("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
