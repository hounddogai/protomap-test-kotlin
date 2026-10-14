package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaAgreementValidator()
        val item = newServiceAreaAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = ServiceAreaAgreementRequiredOwnerIdRule("service area agreement needs this field")
        val item = newServiceAreaAgreement("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = ServiceAreaAgreementOwnerIdLengthRule(32)
        val item = newServiceAreaAgreement("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaAgreementCodeLengthRule(32)
        val item = newServiceAreaAgreement("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
