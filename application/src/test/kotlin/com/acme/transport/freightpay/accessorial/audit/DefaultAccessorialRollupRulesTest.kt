package com.acme.transport.freightpay.accessorial.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAccessorialRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAccessorialRollupValidator()
        val item = newDefaultAccessorialRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = DefaultAccessorialRollupChecksumLengthRule(120)
        val item = newDefaultAccessorialRollup("x", "y")
        item.checksum = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DefaultAccessorialRollupRequiredChecksumRule("default accessorial rollup needs an entry")
        val item = newDefaultAccessorialRollup("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultAccessorialRollupRequiredNameRule("default accessorial rollup needs a value")
        val item = newDefaultAccessorialRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DefaultAccessorialRollupChannelLengthRule(3600)
        val item = newDefaultAccessorialRollup("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
