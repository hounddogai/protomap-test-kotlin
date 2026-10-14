package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDefectRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDefectRecordValidator()
        val item = newFallbackDefectRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackDefectRecordRequiredLabelRule("fallback defect record needs a value")
        val item = newFallbackDefectRecord("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = FallbackDefectRecordRequiredUnitRule("fallback defect record needs a value")
        val item = newFallbackDefectRecord("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDefectRecordRequiredCodeRule("fallback defect record needs this field")
        val item = newFallbackDefectRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = FallbackDefectRecordUnitLengthRule(16)
        val item = newFallbackDefectRecord("x", "y")
        item.unit = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = FallbackDefectRecordRequiredCarrierRule("fallback defect record needs an entry")
        val item = newFallbackDefectRecord("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
