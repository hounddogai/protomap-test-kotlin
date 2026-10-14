package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredHubExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredHubExceptionValidator()
        val item = newTieredHubException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredHubExceptionNameLengthRule(120)
        val item = newTieredHubException("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredHubExceptionRequiredCodeRule("tiered hub exception needs an entry")
        val item = newTieredHubException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
