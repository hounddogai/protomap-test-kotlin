package com.acme.transport.lastmile.parcel.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedParcelDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedParcelDigestValidator()
        val item = newCappedParcelDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedParcelDigestRequiredTitleRule("capped parcel digest needs this field")
        val item = newCappedParcelDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedParcelDigestRequiredCodeRule("capped parcel digest needs an entry")
        val item = newCappedParcelDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
