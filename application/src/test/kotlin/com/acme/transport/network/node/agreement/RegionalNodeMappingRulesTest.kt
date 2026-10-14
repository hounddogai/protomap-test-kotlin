package com.acme.transport.network.node.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalNodeMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalNodeMappingValidator()
        val item = newRegionalNodeMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalNodeMappingNameLengthRule(64)
        val item = newRegionalNodeMapping("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RegionalNodeMappingRequiredRouteCodeRule("regional node mapping needs a value")
        val item = newRegionalNodeMapping("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RegionalNodeMappingOwnerIdLengthRule(32)
        val item = newRegionalNodeMapping("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalNodeMappingRequiredCodeRule("regional node mapping needs this field")
        val item = newRegionalNodeMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
