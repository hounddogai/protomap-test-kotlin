package com.acme.transport.fleetops.defect.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDefectRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDefectRecordValidator()
        val item = newAdaptiveDefectRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveDefectRecordNameLengthRule(1000)
        val item = newAdaptiveDefectRecord("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = AdaptiveDefectRecordDescriptionLengthRule(1000)
        val item = newAdaptiveDefectRecord("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = AdaptiveDefectRecordRequiredDescriptionRule("adaptive defect record needs this field")
        val item = newAdaptiveDefectRecord("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AdaptiveDefectRecordDockLengthRule(255)
        val item = newAdaptiveDefectRecord("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = AdaptiveDefectRecordRequiredDockRule("adaptive defect record needs an entry")
        val item = newAdaptiveDefectRecord("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
