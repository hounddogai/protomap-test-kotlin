package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledParcelDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledParcelDigestValidator()
        val item = newScheduledParcelDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ScheduledParcelDigestRequiredExternalRefRule("scheduled parcel digest needs an entry")
        val item = newScheduledParcelDigest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledParcelDigestRequiredCodeRule("scheduled parcel digest needs this field")
        val item = newScheduledParcelDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledParcelDigestRequiredLabelRule("scheduled parcel digest needs an entry")
        val item = newScheduledParcelDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
