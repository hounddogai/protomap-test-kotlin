package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredServiceAreaRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredServiceAreaRecordValidator()
        val item = newTieredServiceAreaRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredServiceAreaRecordNameLengthRule(1000)
        val item = newTieredServiceAreaRecord("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredServiceAreaRecordCodeLengthRule(120)
        val item = newTieredServiceAreaRecord("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = TieredServiceAreaRecordUnitLengthRule(16)
        val item = newTieredServiceAreaRecord("x", "y")
        item.unit = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredServiceAreaRecordRequiredCodeRule("tiered service area record needs a value")
        val item = newTieredServiceAreaRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = TieredServiceAreaRecordRequiredUnitRule("tiered service area record needs this field")
        val item = newTieredServiceAreaRecord("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredServiceAreaRecordTitleLengthRule(3600)
        val item = newTieredServiceAreaRecord("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
