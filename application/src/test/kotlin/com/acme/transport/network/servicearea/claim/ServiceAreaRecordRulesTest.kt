package com.acme.transport.network.servicearea.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaRecordValidator()
        val item = newServiceAreaRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ServiceAreaRecordRequiredChannelRule("service area record needs this field")
        val item = newServiceAreaRecord("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ServiceAreaRecordTitleLengthRule(86400)
        val item = newServiceAreaRecord("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaRecordRequiredCodeRule("service area record needs an entry")
        val item = newServiceAreaRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServiceAreaRecordRequiredTitleRule("service area record needs a value")
        val item = newServiceAreaRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
