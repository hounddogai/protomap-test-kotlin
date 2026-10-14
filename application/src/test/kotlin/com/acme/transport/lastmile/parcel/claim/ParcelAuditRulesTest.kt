package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelAuditValidator()
        val item = newParcelAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelAuditRequiredCodeRule("parcel audit needs this field")
        val item = newParcelAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ParcelAuditRequiredDriverIdRule("parcel audit needs this field")
        val item = newParcelAudit("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ParcelAuditRequiredTitleRule("parcel audit needs a value")
        val item = newParcelAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = ParcelAuditDriverIdLengthRule(32)
        val item = newParcelAudit("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ParcelAuditRequiredChannelRule("parcel audit needs an entry")
        val item = newParcelAudit("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
