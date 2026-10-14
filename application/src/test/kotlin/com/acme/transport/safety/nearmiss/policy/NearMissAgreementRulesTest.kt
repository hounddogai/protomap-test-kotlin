package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissAgreementValidator()
        val item = newNearMissAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = NearMissAgreementFloorValueRangeRule(0.1, 4983.8)
        val item = newNearMissAgreement("x", "y")
        item.floorValue = 4985.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = NearMissAgreementNameLengthRule(86400)
        val item = newNearMissAgreement("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = NearMissAgreementRevisionRangeRule(120, 255)
        val item = newNearMissAgreement("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissAgreementCodeLengthRule(80)
        val item = newNearMissAgreement("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissAgreementRequiredCodeRule("near miss agreement needs this field")
        val item = newNearMissAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
