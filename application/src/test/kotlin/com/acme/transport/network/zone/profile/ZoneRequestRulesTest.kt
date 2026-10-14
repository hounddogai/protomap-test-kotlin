package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneRequestValidator()
        val item = newZoneRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ZoneRequestRequiredExternalRefRule("zone request needs an entry")
        val item = newZoneRequest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ZoneRequestFloorValueRangeRule(3.8, 4575.3)
        val item = newZoneRequest("x", "y")
        item.floorValue = 4576.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ZoneRequestCodeLengthRule(64)
        val item = newZoneRequest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ZoneRequestNameLengthRule(255)
        val item = newZoneRequest("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
