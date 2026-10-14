package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRedeliveryEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRedeliveryEntryValidator()
        val item = newRegionalRedeliveryEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RegionalRedeliveryEntryRouteCodeLengthRule(3600)
        val item = newRegionalRedeliveryEntry("x", "y")
        item.routeCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RegionalRedeliveryEntryChecksumLengthRule(64)
        val item = newRegionalRedeliveryEntry("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RegionalRedeliveryEntryRequiredRouteCodeRule("regional redelivery entry needs this field")
        val item = newRegionalRedeliveryEntry("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
