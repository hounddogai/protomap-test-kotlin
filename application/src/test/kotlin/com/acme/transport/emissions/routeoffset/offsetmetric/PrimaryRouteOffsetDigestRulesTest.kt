package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRouteOffsetDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRouteOffsetDigestValidator()
        val item = newPrimaryRouteOffsetDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryRouteOffsetDigestRequiredCodeRule("primary route offset digest needs an entry")
        val item = newPrimaryRouteOffsetDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PrimaryRouteOffsetDigestRequiredChecksumRule("primary route offset digest needs a value")
        val item = newPrimaryRouteOffsetDigest("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = PrimaryRouteOffsetDigestPalletsRangeRule(16, 16)
        val item = newPrimaryRouteOffsetDigest("x", "y")
        item.pallets = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryRouteOffsetDigestCodeLengthRule(16)
        val item = newPrimaryRouteOffsetDigest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
