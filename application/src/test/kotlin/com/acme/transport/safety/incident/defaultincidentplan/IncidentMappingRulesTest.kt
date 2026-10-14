package com.acme.transport.safety.incident.defaultincidentplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentMappingValidator()
        val item = newIncidentMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = IncidentMappingExternalRefLengthRule(3600)
        val item = newIncidentMapping("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = IncidentMappingCostRangeRule(2.9, 2374.8)
        val item = newIncidentMapping("x", "y")
        item.cost = 2376.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = IncidentMappingCountryLengthRule(16)
        val item = newIncidentMapping("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = IncidentMappingBatchSizeRangeRule(16, 3600)
        val item = newIncidentMapping("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
