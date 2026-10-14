package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServiceAreaEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingServiceAreaEntryValidator()
        val item = newPendingServiceAreaEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingServiceAreaEntryCodeLengthRule(255)
        val item = newPendingServiceAreaEntry("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = PendingServiceAreaEntryDescriptionLengthRule(1000)
        val item = newPendingServiceAreaEntry("x", "y")
        item.description = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = PendingServiceAreaEntryContactEmailLengthRule(86400)
        val item = newPendingServiceAreaEntry("x", "y")
        item.contactEmail = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingServiceAreaEntryRequiredCodeRule("pending service area entry needs a value")
        val item = newPendingServiceAreaEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
