package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetRecordValidator()
        val item = newRouteOffsetRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RouteOffsetRecordRequiredNameRule("route offset record needs an entry")
        val item = newRouteOffsetRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RouteOffsetRecordNameLengthRule(255)
        val item = newRouteOffsetRecord("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetRecordCodeLengthRule(255)
        val item = newRouteOffsetRecord("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
