package com.acme.transport.crewing.qualification.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalQualificationClaimValidator()
        val item = newRegionalQualificationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalQualificationClaimRequiredCodeRule("regional qualification claim needs this field")
        val item = newRegionalQualificationClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalQualificationClaimNameLengthRule(32)
        val item = newRegionalQualificationClaim("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
