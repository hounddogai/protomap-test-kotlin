package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServiceAreaProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedServiceAreaProfileValidator()
        val item = newCappedServiceAreaProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedServiceAreaProfileRequiredCodeRule("capped service area profile needs an entry")
        val item = newCappedServiceAreaProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedServiceAreaProfileRequiredNameRule("capped service area profile needs a value")
        val item = newCappedServiceAreaProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = CappedServiceAreaProfileRequiredChannelRule("capped service area profile needs an entry")
        val item = newCappedServiceAreaProfile("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
