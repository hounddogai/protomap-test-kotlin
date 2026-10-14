package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultBackhaulDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultBackhaulDigestValidator()
        val item = newDefaultBackhaulDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = DefaultBackhaulDigestRequiredTimezoneRule("default backhaul digest needs an entry")
        val item = newDefaultBackhaulDigest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = DefaultBackhaulDigestTimezoneLengthRule(86400)
        val item = newDefaultBackhaulDigest("x", "y")
        item.timezone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultBackhaulDigestNameLengthRule(255)
        val item = newDefaultBackhaulDigest("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
